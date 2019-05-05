package caso3;

import uniandes.gload.core.Task;

public class ElTask extends Task{

	@Override
	public void fail() {
		System.out.println("DAMNN");
		
	}

	@Override
	public void success() {
		System.out.println("ESOO");
		
	}

	@Override
	public void execute() {
		ClienteCon c = new ClienteCon("localhost",6969,0);
		try {
			c.ejecutar();
		} catch (ProtocoloException e) {
			e.printStackTrace();
		}
	}
	}

