package com.github.catageek.ByteCart;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

// this IC represents a stop/start block
// it is commanded by a wire (like FB 'station' block)
// wire on => start or no velocity change
// wire off => stop
// it provides a busy bit with a lever on the block above the sign
// lever off = block occupied and not powered
// lever on = block free OR powered

public class BC7001 extends AbstractIC implements TriggeredIC, PoweredIC {
	
	private Vehicle Vehicle = null;
	
	final private BC7001 bc7001 = this;

	public BC7001(org.bukkit.block.Block block) {
		super(block);
		this.Name = "BC7001";
		this.FriendlyName = "Stop/Start";
		this.Buildtax = ByteCart.myPlugin.getConfig().getInt("buildtax." + this.Name);
		this.Permission = this.Permission + this.Name;
	}

	@Override
	public void trigger() {
		
		// add input command = redstone
		
		InputPin[] wire = new InputPin[2];
		
		// Right
		wire[0] = InputPinFactory.getInput(this.getBlock().getRelative(BlockFace.UP).getRelative(MathUtil.clockwise(getCardinal())));
		// left
		wire[1] = InputPinFactory.getInput(this.getBlock().getRelative(BlockFace.UP).getRelative(MathUtil.anticlockwise(getCardinal())));

		// InputRegistry[0] = start/stop command
		this.addInputRegistry(new PinRegistry<InputPin>(wire));
		
		// add output occupied line = lever
		
		OutputPin[] lever = new OutputPin[1];
		
		// Right
		lever[0] = OutputPinFactory.getOutput(this.getBlock().getRelative(getCardinal().getOppositeFace()));

		// OutputRegistry[0] = occupied signal
		this.addOutputRegistry(new PinRegistry<OutputPin>(lever));
		
		// here starts the action
		
		// is there a minecart above ?
		if (this.Vehicle != null) {

			
			// if the wire is on
			if(this.getInput(0).getAmount() > 0) {
				
				// the lever is on too
				this.getOutput(0).setAmount(1);

				// if the cart is stopped, start it
				if (this.Vehicle.getVelocity().equals(new Vector(0,0,0))) {

					this.Vehicle.setVelocity((new Vector(this.getCardinal().getModX(), this.getCardinal().getModY(), this.getCardinal().getModZ())).multiply(ByteCart.myPlugin.getConfig().getDouble("BC7001.startvelocity")));
				}
			}
			
			// if the wire is off
			else {
				
				// the lever is off
				this.getOutput(0).setAmount(0);

				// stop the cart
				bc7001.Vehicle.setVelocity(new Vector(0,0,0));
/*
				if(ByteCart.debug)
					ByteCart.log.info("ByteCart: BC7001 : cart on stop at " + this.Vehicle.getLocation().toString());
*/
			}
				
		}
		
		// there is no minecart above
		else {
			// the lever is on
			this.getOutput(0).setAmount(1);
		}

	}
	
	public BC7001 AddVehicle(Vehicle v) {
		this.Vehicle = v;
		return this;
	}

	@Override
	public void power() {
		// power update (TO VERIFY)
		
		// We need to find if a cart is stopped and set the member variable Vehicle
		Location loc = this.getBlock().getRelative(BlockFace.UP, 2).getLocation();
		
		List<Entity> ent = Arrays.asList(this.getBlock().getChunk().getEntities());
/*
		if(ByteCart.debug)
			ByteCart.log.info("ByteCart: BC7001 : loading " + ent.size() + " entities.");
*/
		for (ListIterator<Entity> it = ent.listIterator(); it.hasNext();) {
/*			
			if(ByteCart.debug) {
				ByteCart.log.info("ByteCart: BC7001 : examining entity at " + it.next().getLocation().toString());
				it.previous();
			}
*/
			if (it.next() instanceof Minecart) {
				it.previous();
				
				if ( MathUtil.isSameBlock(((Minecart) it.next()).getLocation(), loc)) {
					it.previous();
					this.AddVehicle((Vehicle) it.next());
/*
					if(ByteCart.debug)
						ByteCart.log.info("ByteCart: BC7001 : cart on stop");
*/					
					break;
					
				}
			}
		}

		this.trigger();
		
		
	}

}
