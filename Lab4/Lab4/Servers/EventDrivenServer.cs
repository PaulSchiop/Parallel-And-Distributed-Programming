using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using static System.Collections.Specialized.BitVector32;

namespace Lab4.Servers
{
    public class EventDrivenServer
    {
        enum State { ResolvingDns, Connecting, SendingRequest, ReadingHeaders, ReadingBody, Done , Error }

        public EventDrivenServer()
        {
        }

        public void Start(string url, string savePath, CountdownEvent countdownEvent)
        {
            _savePath = savePath;
            _countdown = countdownEvent;
            _headerBuilder = new StringBuilder(); 

            try
            {
                Uri uri = new Uri(url);
                _host = uri.Host;
                _path = uri.AbsolutePath;
                _port = uri.Port;

                _state = State.ResolvingDns;

                Dns.BeginGetHostEntry(_host, OnDnsResolved, null);
            }
            catch (Exception ex)
            {
                HandleError($"Setup failed: {ex.Message}");
            }
        }

        private void OnDnsResolved(IAsyncResult ar)
        {
            try 
            { 
                IPHostEntry entry = Dns.EndGetHostEntry(ar);
                IPAddress ip = entry.AddressList.First(addr => addr.AddressFamily == AddressFamily.InterNetwork);

                _conn = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                _state = State.Connecting;
                _conn.BeginConnect(ip, _port, OnConnected, null);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"DNS resolution failed: {ex.Message}");
            }
        }

        private void OnConnected(IAsyncResult ar)
        {
            try
            {
                _conn.EndConnect(ar);
                string request = $"GET {_path} HTTP/1.1\r\nHost: {_host}\r\nConnection: close\r\n\r\n";
                byte[] requestBytes = Encoding.ASCII.GetBytes(request);
                _state = State.SendingRequest;
                _conn.BeginSend(requestBytes, 0, requestBytes.Length, SocketFlags.None, OnRequestSent, null);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Connection failed: {ex.Message}");
                return;
            }
        }

        public void OnRequestSent(IAsyncResult ar)
        {
            try
            {
                int bytesSent = _conn.EndSend(ar);
                _fileStream = new FileStream(_savePath, FileMode.Create, FileAccess.Write);
                _state = State.ReadingHeaders;

                _conn.BeginReceive(_buffer, 0, _buffer.Length, SocketFlags.None, OnDataReceived, null);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Request sending failed: {ex.Message}");
            }
        }

        private void OnDataReceived(IAsyncResult ar)
        {
            try
            {
                _size = _conn.EndReceive(ar);
                _pos = 0;

                if (_size == 0)
                {
                    if (_state == State.ReadingBody && (_totalBodyBytesReceived == _contentLength || _contentLength == -1))
                        CompleteDownload();
                    else
                        HandleError("Connection closed prematurely");
                    return;
                }

                ProcessBuffer();

                if (_state == State.ReadingBody && _totalBodyBytesReceived == _contentLength)
                {
                    CompleteDownload(); 
                }
                else if (_state != State.Done && _state != State.Error)
                {
                    _conn.BeginReceive(_buffer, 0, _buffer.Length, SocketFlags.None, OnDataReceived, null);
                }
            }
            catch (Exception ex)
            {
                HandleError($"Receive failed: {ex.Message}");
            }
        }

        private void CompleteDownload()
        {
            if (_state == State.Done) return; 

            Console.WriteLine($"Success: Downloaded {_savePath} ({_totalBodyBytesReceived} bytes)");
            _state = State.Done;
            _fileStream?.Close();
            _conn?.Close();
            _countdown.Signal(); 
        }

        private void HandleError(string message)
        {
            if (_state == State.Error) return; 

            Console.WriteLine($"Error: {_savePath} - {message}");
            _state = State.Error;
            _fileStream?.Close();
            _conn?.Close();

            try
            {
                if (File.Exists(_savePath))
                {
                    File.Delete(_savePath);
                }
            }
            catch { }

            _countdown.Signal(); 
        }

        private void ProcessBuffer()
        {
            if(_state == State.ReadingHeaders)
            {
                while( _pos < _size)
                {
                    char c = (char)_buffer[_pos++];
                    _headerBuilder.Append(c);


                    string currentHeaders = _headerBuilder.ToString();
                    if (currentHeaders.EndsWith("\r\n\r\n"))
                    {
                        ParseHeaders(currentHeaders);
                        _state = State.ReadingBody;

                        break;
                    }
                }
            }
            
            if (_state == State.ReadingBody)
            {
                int bytesToWrite = _size - _pos;
                if (bytesToWrite > 0)
                {
                    _fileStream.Write(_buffer, _pos, bytesToWrite);
                    _totalBodyBytesReceived += bytesToWrite;
                    _pos = _size;
                }
            }
        }

        private void ParseHeaders(string currentHeaders)
        {
            string[] lines = currentHeaders.Split(new [] { "\r\n" }, StringSplitOptions.None);
            foreach (string line in lines)
            {
                if (line.StartsWith("Content-Length:", StringComparison.OrdinalIgnoreCase))
                {
                    string lengthStr = line.Substring("Content-Length:".Length).Trim();
                    int.TryParse(lengthStr, out _contentLength);
                    Console.WriteLine($"File {_savePath} is {_contentLength} bytes.");
                    return;
                }
            }
            Console.WriteLine($"Warning: Could not find Content-Length for {_savePath}.");
        }

        private Socket _conn;
        private byte[] _buffer = new byte[2048];
        private int _pos;
        private int _size;
        private FileStream _fileStream;
        private State _state;
        private string _host;
        private string _path;
        private int _port;
        private string _savePath;
        private CountdownEvent _countdown;
        private int _totalBodyBytesReceived = 0;
        private int _contentLength = -1;
        private StringBuilder _headerBuilder;
    }
}
