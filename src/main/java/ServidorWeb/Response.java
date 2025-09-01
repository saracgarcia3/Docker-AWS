package ServidorWeb;

import java.nio.charset.StandardCharsets;

public class Response {
    private int status = 200;
    private String contentType = "text/plain; charset=UTF-8";
    private byte[] body = new byte[0];

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getContentType() { return contentType; }
    public void setContentType(String ct) { this.contentType = ct; }

    public byte[] getBodyBytes() { return body; }

    public void setBody(String s) {
        contentType = "text/plain; charset=UTF-8";
        body = (s == null) ? new byte[0] : s.getBytes(StandardCharsets.UTF_8);
    }

    public void setBodyHtml(String html) {
        contentType = "text/html; charset=UTF-8";
        body = (html == null) ? new byte[0] : html.getBytes(StandardCharsets.UTF_8);
    }

    public void setBodyBytes(String ct, byte[] data) {
        contentType = (ct == null) ? "application/octet-stream" : ct;
        body = (data == null) ? new byte[0] : data;
    }
}
