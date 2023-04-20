package kr.hs.dgsw;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    // 클라인언트로 소켓이 요청이 들어오면 관리하는 클래스
    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        int id; // 고유 아이디
        String userName;

        ChatMessage chatMessage; // 메시지 타입
        boolean keepGoing; // Thread 진행 여부

        public ClientThread(Socket socket) {
            id = ++uniqueId;
            keepGoing = true;
            this.socket = socket;
            display("클라이언트 " + socket.getInetAddress() + ":" + socket.getPort() + "접속");
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                userName = inputStream.readUTF();
            } catch (IOException e) {
                display(" " + e);
            }
        }

        @Override
        public void run() { // 해당 ClientThread의 Client로부터 수신된 ChatMessage의 타입별 작업처리
            // InputStream으로 ChatMessage 수신
            while (keepGoing) {
                try {
                    chatMessage = (ChatMessage) inputStream.readObject();
                } catch (IOException e) {
                    display(userName + "님 예외 발생 " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                String message = chatMessage.getMessage();
            } // while
            remove(id); // ArrayList 에서 해당 id를 가진 Thread(요소) 삭제
            close();
        }

        private boolean writeMsg(String msg) { // 클라이언트에게 메시지 전송
            // outputStream으로 메시지 전송
            if (!socket.isConnected()) { // 연결 여부 확인
                close();
                return false;
            }
            try {
                outputStream.writeUTF(msg);
                outputStream.flush();
            } catch (IOException e) {
                display(notIf + "메시지 전송 에러 " + userName + notIf);
                display(e.toString());
            }
            return true;
        }

        public void close() {
            // outputStream, inputStream, socket close
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
                if (socket != null)
                    socket.close();
            } catch (Exception e) {
            }
        }

        public String getUsername() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
    private static int uniqueId; // 번호
    private ArrayList<ClientThread> clientThreads; // 클라이언트 스레드 배열 ArrayList = 동적배열
    private SimpleDateFormat simpleDateFormat; // 특정 문자열 포맷으로 날짜 표현
    private int port; // Server Port
    private boolean isRunning; // 서버 실행 여부
    private String notIf = "***";

    public Server(int port) {
        this.port = port;
        this.simpleDateFormat = new SimpleDateFormat("HH:mm:ss"); // 시 분 초
        this.clientThreads = new ArrayList<>();
    }

    public void start() {
        // 서버 시작 ServerSocket 생성 및 accept
        isRunning = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            display("[서버 시작]");
            while (isRunning) {
                Socket socket = serverSocket.accept(); // 클라이언트 요청 수락
                if(isRunning)
                    break;
                 ClientThread clientThread = new ClientThread(socket);
                 clientThreads.add(clientThread);
                 clientThread.start(); // 스레드 시작
            } // while
        }catch (IOException e) {
            display("서버 예외 발생" + e);
        }
        System.out.println("[서버 종료]");
    }

    public void stop() {
        // 서버 종료
        isRunning =  false;
    }

    private void display(String message) {
        // HH:mm:ss 포맷형식으로 Client에게 message전달
        String time = simpleDateFormat.format(new Date()) + " " + message;
        System.out.println(time); // HH:mm:ss
    }

    private synchronized boolean broadcast(String message) {
        // Server에 연결된 모든 Client에게 message 전달
        String time = simpleDateFormat.format(new Date());
        String msg = time + " " + message;
        // 클라이언트의 연결이 끊어진 경우가 있을 수 있기 때문에 역순으로 조회함
        for(int i = clientThreads.size()-1; i >= 0; i--) {
            ClientThread clientThread = clientThreads.get(i); // 해당 동적배열의 i번째를 받음
            if(!clientThread.writeMsg(msg)) { // writeMsg()의 반환타입이 false면 연결이 끊어진 상태
                clientThreads.remove(i);
                display("Clinet : " + clientThread.userName + "연결 종료.");
            }
        }
        return true;
    }

    private synchronized void remove(int id) {
        // id에 해당하는 클라이언트 연결 종료
        String disconnectedClient = "";
        for(ClientThread clientThread: clientThreads) {
            if(clientThread.id == id) {
                disconnectedClient = clientThread.getUsername();
                clientThreads.remove(clientThread);
                break;
            }
        }
        System.out.println(notIf + disconnectedClient + "님이 채팅방을 나갔습니다." + notIf);
    }
}