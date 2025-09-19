public class FtpUtils{
    private static final Logger log = Logger.getLogger(FtpUtils.class);

    public static FTPClient getFtpClient(String url, int port, String username, String password){
        FTPClient ftpClient = null;
        int reply;

        ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(30000);
        try{
            ftpClient.connect(url, port);
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            if(!FTPReplay.isPostitiveCompletion(reply)){
                ftpClient.disconnect();
                log.error("ftp连接失败！连接地址-------->" + url);
                return null;
            }

            if(FTPReply.isPostiveCompletion(ftpClient.sendCommand(
                "OPTS UTF8", "ON"
            ))){
                ftpClient.setControlEncoding("UTF-8");
            }else{
                ftpClient.setControlEncoding("UTF-8");
            }

        }catch(Exception e){
            log.error("ftp连接出错！连接地址-------->" + url, e);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return ftpClient;
    }

    public static InputStream getFtpFileInputStream(FTPClient ftpClient, String path, String fileName){
        InputStream inputStream = null;

        if(ftpClient == null){
            return inputStream;
        }

        try{
            ftpClient.changeWorkingDirectory(path);
            FTPFile[] listFiles = ftpClient.listFiles();
            if(listFiles.length > 0){
                inputStream = ftpClient.retrieveFileStream(new String(fileName.getBytes("utf-8"), FTP.DEFAULT_CONTROL_ENCODING));
            }
        }catch(Exception e){
            log.error("获取ftp文件输入流出错！文件路径-------->" + path + File.separator + fileName, e);
            System.out.print(e.getMessage());
            e.printStackTrace();
        }finally{
            if(ftpClient != null){
                disConnection(ftpClient);
            }
        }

        return inputStream;
    }

    public static boolean deleteFtpFile(String url, int port, String username, String password, String fileName, String encode){
        FTPClient ftpClient = getFtpClient(url, port, username, password);
        if(ftpClient == null){
            log.error("ftp链接失败！地址为------->" + url + port);
        }

        boolean isFlag = false;

        List<String> fileName = new ArrayList();
        try{
            ftpClient.changeWorkingDirectory("/");
            ftpClient.dele(fileName);
            isFlag = true;
        }catch(Exception e){
            log.error("删除ftp文件出错！地址为-------->" + url + ":" + port + File.separator + fileName, e);
            System.out.print(e.getMessage());
            e.printStackTrace();
        }finally{
            if(ftpClient != null){
                disConnection(ftpClient);
            }
        }
        return isFlag;
    }

    public static List<String> getFtpAllFile(String url, int port, String username, String password, String path, String encode){
        int i = 0;
        FTPClient ftpClient = getFtpClient(url, port, username, password);
        if(ftpClient == null){
            log.error("ftp链接失败！地址为---->" + url + port);
        }
        BufferedReader br = null;
        InputStream in = null;
        return getFtpFAllFileName(ftpClient, path);
    }

    private static List<String> getFtpFAllFileName(FTPClient ftpClient, String path){
        List<String> fileNames = new ArrayList();
        try{
            FTPFile[] listFiles = ftpClient.listFiles();
            if(listFiles.length > 0){
                for(int i = 0;  i < listFiles.length; i ++){
                    if(listFiles[i].isFile()){
                        String fileName = new String(listFiles[i].getName().getBytes("gbk"), "utf-8"); //win下默认为gbk
                        fileNames.add(fileName);
                    }
                }
            }
        }catch(Exception e){
            log.error("获取ftp所有文件名称出错！地址为-------->" + path, e);
            System.out.print(e.getMessage());
            e.printStackTrace();
        }finally{
            if(ftpClient != null){
                disConnection(ftpClient);
            }
        }
        return fileNames;
    }

    
}