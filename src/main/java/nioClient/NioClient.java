package nioClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioClient {
	//通道管理器
	private Selector selector;
	
	/**
	 * @Description 获得一个通道，并对该通道做一些初始化的工作
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public void initClient(String ip, int port) throws IOException {
		//获取一个socket通道
		SocketChannel channel = SocketChannel.open();
		//设置通道为非阻塞
		channel.configureBlocking(false);
		//获得一个通道管理器
		this.selector = Selector.open();
		
		//客户端连接服务器，其方法执行并没有实现连接，需要在listen()方法中调用
		//channel.finishConnect();才能连接
		channel.connect(new InetSocketAddress(ip,port));
		//将通道管理器和该通道绑定，并为该通道注册selectionKey.OP_CONNECT事件。
		channel.register(selector, SelectionKey.OP_CONNECT);
	} 
	
	/**
	 * @Descrition  采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理 
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public void listen() throws IOException {
		//轮询selector
		while(true) {
			selector.select();
			//获得selector中选中项的的迭代器
			Iterator ite = this.selector.selectedKeys().iterator();
			while(ite.hasNext()) {
				SelectionKey key = (SelectionKey)ite.next();
				//删除已选的key，防止重复处理
				ite.remove();
				//连接事件发生
				if(key.isConnectable()) {
					SocketChannel channel = (SocketChannel)key.channel();
					//如果正在连接，则完成连接
					if(channel.isConnectionPending()) {
						channel.finishConnect();
					}
					//设置为非阻塞
					channel.configureBlocking(false);
					//给服务端发送消息
					channel.write(ByteBuffer.wrap(new String("给服务端发送消息！！！！").getBytes()));
					//在和服务端连接成功后，为了能接收到服务端的消息，需要给通道设置读的权限。
					channel.register(this.selector, SelectionKey.OP_READ);
				}else if (key.isReadable()) {
					read(key);
				}
			}
		}
	}
	
	/**
	 * @Description 处理读取服务端发来的消息事件
	 * @param key
	 * @throws IOException
	 */
	public void read(SelectionKey key) throws IOException {
		// 服务器可读消息：得到事件发生的的socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(10);
		channel.read(buffer);
		byte[] data = buffer.array();
		String msg = new String(data).trim();
		System.out.println("服务端收到消息！！！！！");
		ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
		// 将消息发送给客户端
		channel.write(outBuffer);
	}
	
	
	public static void main(String args[]) throws IOException {
		NioClient nioClient = new NioClient();
		nioClient.initClient("localhost", 8000);
		nioClient.listen();
	}
	
	

}
