package awesome.oauth;


public class Runner {
	
	public static void main(String[] args) {
		try {
			
			Server app = new Server();
			app.start();
			
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}

	}

}