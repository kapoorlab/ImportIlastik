package utils;

import java.util.ArrayList;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;

public class StringImage {
	
	public final String ID;
	public final String IDSec;
	public IntervalView<UnsignedShortType> image;
	
	public StringImage ( final String ID, final IntervalView<UnsignedShortType> image) {
		
		this.ID = ID;
		this.IDSec = null;
		this.image = image;
		
		
	}
	
public StringImage ( final String ID, final String IDSec, final IntervalView<UnsignedShortType> image) {
		
		this.ID = ID;
		this.IDSec = IDSec;
		this.image = image;
		
		
	}
	

}
