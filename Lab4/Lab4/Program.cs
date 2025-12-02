using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Lab4.Servers; 

public class Program
{
    private static List<string> urls = new List<string>
    {
        "https://www.cs.ubbcluj.ro/~rlupsa/edu/pdp/progs/srv-begin-end.cs",
        "https://www.cs.ubbcluj.ro/~rlupsa/edu/pdp/progs/srv-await.cs",
        "http://www.nonexistentdomain12345.com/testdsf.txt"
    };

    public static void Main(string[] args)
    {
        Console.WriteLine("Choose implementation to run:");
        Console.WriteLine("1. Event-Driven (Callbacks)");
        Console.WriteLine("2. Task-based (ContinueWith)");
        Console.WriteLine("3. Task-based (async/await)");
        Console.Write("Enter choice (1-3): ");

        string choice = Console.ReadLine();

        switch (choice)
        {
            case "1":
                RunCallbackModel();
                break;
            case "2":
                RunContinueWithModel();
                break;
            case "3":
                RunAsyncAwaitModel();
                break;
            default:
                Console.WriteLine("Invalid choice.");
                break;
        }

        Console.WriteLine("--- All Operations Finished ---");
        Console.WriteLine("Press any key to exit.");
        Console.ReadKey();
    }

    public static void RunCallbackModel()
    {
        Console.WriteLine("\n--- Starting All Downloads (Pure Callback Model) ---");
        CountdownEvent allDownloadsComplete = new CountdownEvent(urls.Count);

        foreach (var url in urls)
        {
            string savePath = GetSavePath(url);
            if (savePath == null)
            {
                allDownloadsComplete.Signal(); 
                continue;
            }

            EventDrivenServer downloader = new EventDrivenServer();
            downloader.Start(url, savePath, allDownloadsComplete);
        }

        allDownloadsComplete.Wait(); 
        allDownloadsComplete.Dispose();
    }

    public static void RunContinueWithModel()
    {
        return;
    }

    public static void RunAsyncAwaitModel()
    {
        Console.WriteLine("\n--- Starting All Downloads (Async/Await Model) ---");
        List<Task> allDownloadTasks = new List<Task>();

        foreach (var url in urls)
        {
            string savePath = GetSavePath(url);
            if (savePath == null) continue;

            AsyncAwaitServer downloader = new AsyncAwaitServer();
            allDownloadTasks.Add(downloader.StartDownloadAsync(url, savePath));
        }

        Task.WhenAll(allDownloadTasks).Wait();
    }

    private static string GetSavePath(string url)
    {
        try
        {
            string host = new Uri(url).Host;
            string fileName = Path.GetFileName(new Uri(url).AbsolutePath);
            if (string.IsNullOrEmpty(fileName) || fileName == "/")
            {
                fileName = host + ".html";
            }
            return Path.Combine(Environment.CurrentDirectory, fileName);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Invalid URL {url}: {ex.Message}");
            return null;
        }
    }
}