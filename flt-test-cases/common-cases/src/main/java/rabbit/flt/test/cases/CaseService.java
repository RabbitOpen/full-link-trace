package rabbit.flt.test.cases;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import rabbit.flt.common.Traceable;

import java.io.IOException;

public class CaseService {

    @Traceable
    public int recursivelyAdd(int d) {
        d += 10;
        count();
        if (d < 50) {
            return recursivelyAdd(d);
        } else {
            return d;
        }
    }

    @Traceable
    private void count() {};

    @Traceable
    public void longTrace(int count) {
        for (int i = 0; i < count; i++) {
            count();
        }
    }

    @Traceable
    public void doHttp3Request() throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod("http://localhost:8888/mvc/hello");
        method.addRequestHeader("name", "zhangsan");
        client.executeMethod(method);
    }

    @Traceable
    public void doHttp4Request() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet request = new HttpGet("http://localhost:8888/mvc/hello?age=10");
            request.addHeader("name", "zhang3");
            client.execute(request);
        } finally {
            client.close();
        }
    }

    @Traceable
    public String callError() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = client.execute(new HttpGet("http://localhost:8888/mvc/error"));
            return EntityUtils.toString(response.getEntity());
        } finally {
            client.close();
        }
    }
}
