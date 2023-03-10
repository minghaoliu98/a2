import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;


@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {
    private final String QUEUE_NAME_1 = "Queue1";
    private final String QUEUE_NAME_2 = "Queue2";
    private final String RABBITMQ_URL = "34.221.204.214";
    private final int NUM_THREAD = 100;
    private RMQChannelPool pool;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        sendQueue("HelloWorld");
        res.getWriter().write("It works!");
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);

    }
    @Override
    public void init() throws ServletException {
        super.init();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_URL);
        factory.setVirtualHost("mark");
        try {
            Connection connection = factory.newConnection();
            RMQChannelFactory channelFactory = new RMQChannelFactory(connection);
            pool = new RMQChannelPool(NUM_THREAD, channelFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void sendQueue(String content) {
        try {
            Channel channel;
            channel = pool.borrowObject();
            channel.basicPublish("", QUEUE_NAME_1, null, content.getBytes());
            channel.basicPublish("", QUEUE_NAME_2, null, content.getBytes());
            pool.returnObject(channel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String jsonString = ReadBigStringIn(req.getReader());
        JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
        res.setContentType("text/plain");
        if (validation(json)) {
            sendQueue(json.toString());
            res.setStatus(201);
        } else {
            res.setStatus(400);
        }
    }


    public boolean validation(JsonObject json) {
        try {
            String swipe = json.get("swipe").getAsString();
            if (!swipe.equals("Left") && !swipe.equals("Right")) {
                return false;
            }
            if (json.get("swiper").getAsInt() < 1 || json.get("swiper").getAsInt() > 5000) {
                return false;
            }
            if (json.get("swipee").getAsInt() < 1 || json.get("swipee").getAsInt() > 1000000) {
                return false;
            }
            if (json.get("comment").getAsString().length() != 256) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public String ReadBigStringIn(BufferedReader buffIn) throws IOException {
        StringBuilder everything = new StringBuilder();
        String line;
        while( (line = buffIn.readLine()) != null) {
            everything.append(line);
        }
        return everything.toString();
    }
}
