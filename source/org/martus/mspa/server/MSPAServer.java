package org.martus.mspa.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWords;
import org.martus.common.Version;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.network.NetworkInterfaceConstants;
import org.martus.mspa.network.NetworkInterfaceXmlRpcConstants;
import org.martus.mspa.network.ServerSideHandler;


public class MSPAServer implements NetworkInterfaceXmlRpcConstants
{
		
	public MSPAServer(File dir) throws Exception
	{	
		mspaHandler = new ServerSideHandler(this);								
		initalizeFileDatabase(dir);		
		logger = new LoggerToConsole();	
		magicWords = new MagicWords(logger);	
		magicWords.loadMagicWords(getMagicWordsFile());
	}	
	
	private void initalizeFileDatabase(File dir)
	{
		serverDirectory = dir;				

		security = loadMartusKeypair(getMSPAServerKeyPairFile());
		martusDatabaseToUse = new ServerFileDatabase(getPacketDirectory(), security);		

		try
		{
			martusDatabaseToUse.initialize();
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map File");
			System.exit(7);
		}
		catch(FileDatabase.MissingAccountMapSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map Signature File");
			System.exit(7);
		}
		catch(FileVerificationException e)
		{
			e.printStackTrace();
			System.out.println("Account Map did not verify against signature file");
			System.exit(7);
		}		
	}	
	
	public MartusCrypto loadMartusKeypair(String keyPairFileName)
	{
		return MartusServerUtilities.loadKeyPair(keyPairFileName, true);		
	}
	
	public String getMSPAServerKeyPairFile()
	{
		return getConfigDirectory()+KEYPAIR_FILE;
	}
	
	public File getMartusServerMagicWordFile()
	{
		return new File(getMartusConfigDirectory(),MAGICWORDS_FILENAME);
	}	
	
	public File getPacketDirectory()
	{
		return new File(getServerDirectory(), "packets");
	}
	
	public File getServerDirectory()
	{
		return serverDirectory;
	}		
	
	public static File getServerWhoWeCallDirectory()
	{
		return new File(getAppDirectoryPath(),"ServersWhoWeCall");
	}
	
	public static File getServerWhoCallUsDirectory()
	{
		return new File(getAppDirectoryPath(),"ServersWhoCallUs");
	}
	
	public static File getAmplifyWhoCallUsDirectory()
	{
		return new File(getAppDirectoryPath(),"AmplifyWhoCallUs");
	}
	
	public static File getAvailableMirrorServerDirectory()
	{
		return new File(getAppDirectoryPath(),"AvailableMirrorServers");
	}
	
	public File getConfigDirectory()
	{
		return new File(getAppDirectoryPath(),ADMIN_MSPA_CONFIG_DIRECTORY);
	}
	
	public File getMartusConfigDirectory()
	{
		return new File(getServerDirectory(),ADMIN_MARTUS_CONFIG_DIRECTORY);
	}
	
	public File getMagicWordsFile()
	{
		return new File(getConfigDirectory(), MAGICWORDS_FILENAME);
	}
	
	public MagicWords getMagicWordsInfo()
	{
		return magicWords;
	}
	
	public void updateManagingMirrorServerInfo(Vector mirrorInfo, int mirrorType)
	{
		File sourceDirectory = MSPAServer.getAvailableMirrorServerDirectory();
		File destDirectory = MSPAServer.getMirrorDirectory(mirrorType);	
		deleteAllFilesFromMirrorDirectory(destDirectory.listFiles());
		
		try 
		{			 
			for (int i =0; i<mirrorInfo.size();i++)
			{
				String file = (String) mirrorInfo.get(i);
				copyFile(new File(sourceDirectory, file), new File(destDirectory, file));
			}	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}	
	}		
	
	void deleteAllFilesFromMirrorDirectory(File[] files)
	{
		for (int i=0;i<files.length;i++)
			files[i].delete();
	}
	
	void copyFile(File in, File out) throws Exception 
	{
		 FileChannel sourceChannel = new FileInputStream(in).getChannel();
		 FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		 sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		
		 sourceChannel.close();
		 destinationChannel.close();
	 }
	
	public void updateMagicWords(Vector words)
	{				
		try
		{			
			File backUpFile = new File(getMagicWordsFile().getPath() + ".bak");
			copyFile(getMagicWordsFile(), backUpFile);
			magicWords.writeMagicWords(getMagicWordsFile(), words);
			magicWords.loadMagicWords(getMagicWordsFile());
			
			copyFile(getMagicWordsFile(),getMartusServerMagicWordFile());
		
		}
		catch (Exception ieo)
		{	
			logger.log("MagicWord.txt file not found."+ ieo.toString());		
		}	
	}	
	
	public File getMartusMagicWordsFile()
	{
		return new File(getMartusConfigDirectory(), MAGICWORDS_FILENAME);
	}		
	
	public void createMSPAXmlRpcServerOnPort(int port) throws Exception
	{				
		MartusSecureWebServer.security = getSecurity();
		MartusXmlRpcServer.createSSLXmlRpcServer(getMSPAHandler(),serverObjectName, port, getMainIpAddress());
	}
	
	public ServerSideHandler getMSPAHandler()
	{
		return mspaHandler;
	}
	
	public LoggerInterface getLogger()
	{
		return logger;
	}	
	
	public InetAddress getMainIpAddress() throws UnknownHostException
	{
		return InetAddress.getByName(ipAddress);
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public ServerFileDatabase getDatabase()
	{		
		return martusDatabaseToUse;
	}
	
	boolean isAuthorizeClient(String myAccountId)
	{
		return authorizedClients.contains(myAccountId);
	}	
	
	public String ping()
	{
		return "" + NetworkInterfaceConstants.VERSION;
	}
	
	public static File getMartusDefaultDataDirectory()
	{
		return new File(MSPAServer.getMartusDefaultDataDirectoryPath());
	}	
	
	public static String getMartusDefaultDataDirectoryPath()
	{
		String dataDirectory = null;
		if(Version.isRunningUnderWindows())
			dataDirectory = WINDOW_MARTUS_ENVIRONMENT;
		else
			dataDirectory = System.getProperty("user.home")+UNIX_MARTUS_ENVIRONMENT;
		return dataDirectory;
	}
	
	
	public static File getAppDirectoryPath()
	{
		String appDirectory = null;
		if(Version.isRunningUnderWindows())
			appDirectory = WINDOW_MSPA_ENVIRONMENT;
		else
			appDirectory = System.getProperty("user.home")+UNIX_MSPA_ENVIRONMENT;
		return new File(appDirectory);
	}	
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}

	public int getPortToUse()
	{
		return (portToUse <= 0)? DEFAULT_PORT:portToUse;
	}

	public void setListenersIpAddress(String ipAddr)
	{
		ipAddress = ipAddr;
	}	
	
	
	public void processCommandLine(String[] args)
	{	
		String listenersIpTag = "--listener-ip=";	
		String portToListenTag = "--port=";
		
		System.out.println("");
		for(int arg = 0; arg < args.length; ++arg)
		{
			String argument = args[arg];
			
			if(argument.startsWith(listenersIpTag))
			{	
				String ip = argument.substring(listenersIpTag.length());
				setListenersIpAddress(ip);
				System.out.println("Listener IP to use: "+ ip);
			}
				
			if(argument.startsWith(portToListenTag))
			{	
				String portToUse = argument.substring(portToListenTag.length());
				setPortToUse(Integer.parseInt(portToUse));	
				System.out.println("Port to use for clients: "+ getPortToUse());
			}
		}
		System.out.println("");
	}
	
	public static File getMirrorDirectory(int type)
	{		
		if (type == ManagingMirrorServerConstants.SERVER_WHO_WE_CALL)
			return getServerWhoWeCallDirectory();
		
		if (type == ManagingMirrorServerConstants.WHO_CALLS_US)
			return getServerWhoCallUsDirectory();
			
		return getAmplifyWhoCallUsDirectory();		
	}	
	
	public static void main(String[] args)
	{
		System.out.println("MSPA Server");
		try
		{					
			System.out.println("Setting up socket connection for listener ...");
			
			MSPAServer server = new MSPAServer(MSPAServer.getMartusDefaultDataDirectory());
			server.processCommandLine(args);			
			server.createMSPAXmlRpcServerOnPort(server.getPortToUse());																				
			System.out.println("Waiting for connection...");
		
		}
		catch(Exception e) 
		{
			System.out.println("UnknownHost Exception" + e);
			System.exit(1);			
		}
	}	
		
	ServerSideHandler mspaHandler;
	String ipAddress;
	int portToUse;
	Vector authorizedClients;
	ServerFileDatabase martusDatabaseToUse;	
	MartusCrypto security;
	LoggerInterface logger;
	MagicWords magicWords;
		
	private File serverDirectory;	
	private final static String ADMIN_MSPA_CONFIG_DIRECTORY = "accountConfig";
	private final static String ADMIN_MARTUS_CONFIG_DIRECTORY = "deleteOnStartup";
	private final static String MAGICWORDS_FILENAME = "magicwords.txt";

	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
	private final static String WINDOW_MARTUS_ENVIRONMENT = "C:/MartusServer/";
	private final static String UNIX_MARTUS_ENVIRONMENT = "/var/MartusServer/";
	private final static String WINDOW_MSPA_ENVIRONMENT = "C:/MSPAServer/";
	private final static String UNIX_MSPA_ENVIRONMENT = "/var/MSPAServer/";
	
	private final static int DEFAULT_PORT = 443;
	
}
