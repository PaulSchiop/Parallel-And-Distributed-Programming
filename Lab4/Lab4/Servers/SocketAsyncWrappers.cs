using System;
using System.Net;
using System.Net.Sockets;
using System.Threading.Tasks;

public static class SocketAsyncWrappers
{
    public static Task<IPHostEntry> DnsGetHostEntryAsync(string host)
    {
        var tcs = new TaskCompletionSource<IPHostEntry>();

        Dns.BeginGetHostEntry(host, ar => {
            try
            {
                tcs.SetResult(Dns.EndGetHostEntry(ar));
            }
            catch (Exception ex)
            {
                tcs.SetException(ex);
            }
        }, null);

        return tcs.Task;
    }

    public static Task ConnectAsync(Socket socket, IPAddress ip, int port)
    {
        var tcs = new TaskCompletionSource<object>(); 

        socket.BeginConnect(ip, port, ar => {
            try
            {
                socket.EndConnect(ar);
                tcs.SetResult(null);
            }
            catch (Exception ex)
            {
                tcs.SetException(ex);
            }
        }, null);

        return tcs.Task;
    }

    public static Task<int> SendAsync(Socket socket, byte[] buffer, int offset, int size)
    {
        var tcs = new TaskCompletionSource<int>();

        socket.BeginSend(buffer, offset, size, SocketFlags.None, ar => {
            try
            {
                tcs.SetResult(socket.EndSend(ar));
            }
            catch (Exception ex)
            {
                tcs.SetException(ex);
            }
        }, null);

        return tcs.Task;
    }

    public static Task<int> ReceiveAsync(Socket socket, byte[] buffer, int offset, int size)
    {
        var tcs = new TaskCompletionSource<int>();

        socket.BeginReceive(buffer, offset, size, SocketFlags.None, ar => {
            try
            {
                tcs.SetResult(socket.EndReceive(ar));
            }
            catch (Exception ex)
            {
                tcs.SetException(ex);
            }
        }, null);

        return tcs.Task;
    }
}