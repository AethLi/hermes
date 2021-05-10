class SSEClientExample {
    /**
     * 获取SSE输入流。
     *
     * @param urlPath
     * @return
     * @throws IOException
     */
    static InputStream getSseInputStream(String urlPath) throws IOException {
        URL url = new URL(urlPath)
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection()
        // 这儿根据自己的情况选择get或post
        urlConnection.setRequestMethod("GET")
        urlConnection.setDoOutput(true)
        urlConnection.setDoInput(true)
        urlConnection.setUseCaches(false)
        urlConnection.setRequestProperty("Connection", "Keep-Alive")
        urlConnection.setRequestProperty("Charset", "UTF-8")
        //读取过期时间（很重要，建议加上）
        urlConnection.setReadTimeout(60 * 1000)
        // text/plain模式
        urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
        InputStream inputStream = urlConnection.getInputStream()
        InputStream is = new BufferedInputStream(inputStream)
        return is
    }

    /**
     * 读取数据。
     *
     * @param is
     * @param ansMsgHandler
     * @throws IOException
     */
    static void readStream(InputStream is, AnsMsgHandler ansMsgHandler) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            String line
            while ((line = reader.readLine()) != null) {
                // 处理数据接口
                ansMsgHandler.actMsg(is, line)
            }
            // 当服务器端主动关闭的时候，客户端无法获取到信号。现在还不清楚原因。所以无法执行的此处。
            reader.close()
        } catch (IOException e) {
            e.printStackTrace()
            throw new IOException("关闭数据流！")
        }
    }

    static void main(String[] args) {
//        String urlPath = "http://127.0.0.1:8080/sync/sse/subscribe?theme=test&id=" + UUID.randomUUID().toString()
        String urlPath = "http://127.0.0.1:8080/sync/sse/subscribe?themeName=test&id=1"
        InputStream inputStream = getSseInputStream(urlPath)
        readStream(inputStream, new AnsMsgHandler() {

            void actMsg(InputStream is, String line) {
                System.out.println(line)
            }
        })
    }

    interface AnsMsgHandler {

        void actMsg(InputStream is, String line);

    }
}
