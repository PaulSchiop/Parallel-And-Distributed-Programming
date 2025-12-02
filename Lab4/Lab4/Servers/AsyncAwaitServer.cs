using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Lab4.Servers
{
    public class AsyncAwaitServer
    {
        public async Task StartDownloadAsync(string url, string savePath)
        {
            Socket? socket = null;
            FileStream? fileStream = null;

            try
            {
                // 1. Parse URL
                Uri uri = new Uri(url);
                string host = uri.Host;
                Console.WriteLine($"Starting: {host}{uri.AbsolutePath}");

                // 2. DNS Resolution
                IPHostEntry entry = await SocketAsyncWrappers.DnsGetHostEntryAsync(host);
                IPAddress ip = entry.AddressList.First(addr => addr.AddressFamily == AddressFamily.InterNetwork);

                // 3. Connect
                socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                await SocketAsyncWrappers.ConnectAsync(socket, ip, uri.Port);

                // 4. Send Request
                string request = $"GET {uri.AbsolutePath} HTTP/1.1\r\n" +
                                 $"Host: {host}\r\n" +
                                 $"Connection: close\r\n" +
                                 "\r\n";
                byte[] requestBytes = Encoding.ASCII.GetBytes(request);
                await SocketAsyncWrappers.SendAsync(socket, requestBytes, 0, requestBytes.Length);

                // 5. Receive Headers
                byte[] buffer = new byte[2048];
                StringBuilder headerBuilder = new StringBuilder();
                int contentLength = -1;
                int totalBytesReceived = 0;

                while (true)
                {
                    int bytesRead = await SocketAsyncWrappers.ReceiveAsync(socket, buffer, 0, buffer.Length);
                    if (bytesRead == 0)
                    {
                        throw new Exception("Connection closed prematurely while reading headers.");
                    }

                    string chunk = Encoding.ASCII.GetString(buffer, 0, bytesRead);
                    headerBuilder.Append(chunk);
                    string headers = headerBuilder.ToString();
                    int headerEndIndex = headers.IndexOf("\r\n\r\n");

                    if (headerEndIndex != -1)
                    {
                        string headerBlock = headers.Substring(0, headerEndIndex);
                        contentLength = ParseContentLength(headerBlock);
                        Console.WriteLine($"File {savePath} is {contentLength} bytes.");

                        int bodyStartIndex = headerEndIndex + 4; 
                        byte[] headerBytes = Encoding.ASCII.GetBytes(headers.Substring(0, bodyStartIndex));
                        int bodyBytesRead = bytesRead - headerBytes.Length;

                        if (bodyBytesRead > 0)
                        {
                            fileStream = new FileStream(savePath, FileMode.Create, FileAccess.Write);
                            fileStream.Write(buffer, headerBytes.Length, bodyBytesRead);
                            totalBytesReceived += bodyBytesRead;
                        }

                        break;
                    }
                }

                // 6. Receive Body
                if (fileStream == null) 
                {
                    fileStream = new FileStream(savePath, FileMode.Create, FileAccess.Write);
                }

                while (totalBytesReceived < contentLength || contentLength == -1)
                {
                    int bytesRead = await SocketAsyncWrappers.ReceiveAsync(socket, buffer, 0, buffer.Length);
                    if (bytesRead == 0)
                    {
                        break;
                    }

                    fileStream.Write(buffer, 0, bytesRead);
                    totalBytesReceived += bytesRead;
                }

                Console.WriteLine($"Success: Downloaded {savePath} ({totalBytesReceived} bytes)");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error: {savePath} - {ex.Message}");
                if (File.Exists(savePath)) File.Delete(savePath);
            }
            finally
            {
                fileStream?.Close();
                socket?.Close();
            }
        }

        private int ParseContentLength(string headerBlock)
        {
            string[] lines = headerBlock.Split(new[] { "\r\n" }, StringSplitOptions.None);
            foreach (string line in lines)
            {
                if (line.StartsWith("Content-Length:", StringComparison.OrdinalIgnoreCase))
                {
                    string lengthStr = line.Substring("Content-Length:".Length).Trim();
                    int.TryParse(lengthStr, out int length);
                    return length;
                }
            }
            return -1; 
        }
    }
}
