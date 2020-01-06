package nioServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Description NIO服务端
 * @author dtc
 *
 */
public class NioServer {
	//通道管理器
	private Selector selector;
	
	public void initServer(int port) throws IOException {
		//获得一个ServerSocket通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		//设置通道为非阻塞
		serverSocketChannel.configureBlocking(false);
		//将该通道对应的ServerSocket绑定到port端口
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		//获得一个通道管理器
		this.selector = Selector.open();
		//将通道管理器和通道绑定，并为该通道注册Selection.OP_ACCEPT事件，注册该事件后，
		//当该事件到达时，selector.select()会返回，如果该事件没达到selector.select()会一直阻塞
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	
	/**
	 * @Description 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public void listen() throws IOException {
		System.out.println("服务端启动成功！");
		//轮询访问selector
		while(true) {
			//当注册的事件到达时，方法返回；否则该方法会一直阻塞
			selector.select();
			//获取到selector中选中的项的迭代器，选中的项为注册事件
			Iterator ite = this.selector.selectedKeys().iterator();
			while(ite.hasNext()) {
				SelectionKey key = (SelectionKey)ite.next();
				//删除已选的key,防止重复处理
				ite.remove();
				//客户端请求连接事件
				if(key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel)key.channel();
					//获得和客户端连接的通道
					SocketChannel channel = server.accept();
					//设置成非阻塞
					channel.configureBlocking(false);
					
					//给客户端发送消息
					channel.write(ByteBuffer.wrap(new String("向客户端发送消息！！！！！！！！").getBytes()));
					//在和客户端连接成功后，为了可以接收到客户端的消息，需要给通道设置读的权限
					channel.register(this.selector, SelectionKey.OP_READ);
				}else if (key.isReadable()) {
					read(key);
				}
			}
		}
	}
	
	/**
	 * @Description 处理读取客户端发来的信息的事件
	 * @param key
	 * @throws IOException
	 */
	public void read(SelectionKey key) throws IOException {
		//服务器可读消息：得到事件发生的的socket通道
		SocketChannel channel = (SocketChannel)key.channel();
		//创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(10);
		channel.read(buffer);
		byte[] data = buffer.array();
		String msg = new String(data).trim();
		System.out.println("服务端收到消息！！！！！");
		ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
		//将消息发送给客户端
		channel.write(outBuffer);
	}
	
	/**
	 * @Description 启动服务端测试
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		NioServer server = new NioServer();
		server.initServer(8000);
		server.listen();
	}
	
}
