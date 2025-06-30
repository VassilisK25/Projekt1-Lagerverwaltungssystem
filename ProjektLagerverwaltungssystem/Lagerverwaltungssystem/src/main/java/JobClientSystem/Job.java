package JobClientSystem;

public class Job {
    private String id;
    private String request;
    private String response;
    private String addr;
    private int port;
    private long duration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", request='" + request + '\'' +
                ", response='" + response + '\'' +
                ", addr='" + addr + '\'' +
                ", port=" + port +
                ", duration=" + duration +
                '}';
    }
}

