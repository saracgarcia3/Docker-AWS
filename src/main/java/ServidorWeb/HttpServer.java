package ServidorWeb;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;

public class HttpServer {

    private static final Map<String, BiFunction<Request, Response, String>> getRoutes = new HashMap<>();
    public static void get(String path, BiFunction<Request, Response, String> handler) { getRoutes.put(path, handler); }

    private static String staticDir = "src/main/resources"; // Maven resources folder
    public static void staticfiles(String folder) { staticDir = (folder != null && !folder.isEmpty()) ? folder : staticDir; }

    public static void start(int port, WebRouter router) throws IOException {
        staticfiles("src/main/resources");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor corriendo en http://localhost:" + port);

            get("/App/hello", (req, res) -> {
                List<String> names = req.getValues("name");
                return names.isEmpty() ? "Hello world" : "Hello " + String.join(", ", names);
            });
            get("/App/pi", (req, res) -> String.valueOf(Math.PI));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket, router);
            }
        }
    }

    private static void handleClient(Socket clientSocket, WebRouter router) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String rawPath = tokens[1];
            String basePath = rawPath.split("\\?", 2)[0];

            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {}

            Request req = new Request(method, rawPath);
            Response res = new Response();

            if ("GET".equalsIgnoreCase(method)) {
                WebRouter.Route r = router.match(basePath);
                if (r != null) {
                    Object result = router.invoke(r, req);
                    if (r.produces.startsWith("text/html")) res.setBodyHtml(String.valueOf(result));
                    else if (r.produces.startsWith("text/")) { res.setBody(String.valueOf(result)); res.setContentType(r.produces + "; charset=UTF-8"); }
                    else { res.setBodyBytes(r.produces, String.valueOf(result).getBytes(StandardCharsets.UTF_8)); }
                    sendResponse(out, res);
                    return;
                }

                BiFunction<Request, Response, String> handler = getRoutes.get(basePath);
                if (handler != null) {
                    String result = handler.apply(req, res);
                    if (res.getBodyBytes().length == 0) res.setBody(result);
                    sendResponse(out, res);
                    return;
                }
            }

            serveStaticFile(basePath, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendResponse(OutputStream out, Response res) throws IOException {
    byte[] body = res.getBodyBytes();
    String headers = "HTTP/1.1 " + res.getStatus() + " OK\r\n" +
                     "Content-Type: " + res.getContentType() + "\r\n" +
                     "Content-Length: " + body.length + "\r\n" +
                     "Connection: close\r\n\r\n";
    out.write(headers.getBytes(StandardCharsets.UTF_8));
    out.write(body);
    out.flush();
}

    private static void serveStaticFile(String path, OutputStream out) throws IOException {
        String safePath = path.equals("/") ? "index.html" : (path.startsWith("/") ? path.substring(1) : path);
        File file = new File(staticDir, safePath);

        Response res = new Response();
        if (!file.exists() || file.isDirectory()) {
            res.setStatus(404);
            res.setBody("404 Not Found");
            sendResponse(out, res);
            return;
        }

        byte[] content = Files.readAllBytes(file.toPath());
        res.setBodyBytes(guessContentType(safePath), content);
        sendResponse(out, res);
    }

    private static String guessContentType(String p) {
        String s = p.toLowerCase();
        if (s.endsWith(".html") || s.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (s.endsWith(".css")) return "text/css; charset=UTF-8";
        if (s.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (s.endsWith(".png")) return "image/png";
        if (s.endsWith(".jpg") || s.endsWith(".jpeg")) return "image/jpeg";
        if (s.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
