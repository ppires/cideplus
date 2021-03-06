package cideplus.model;

public class RGB {

	final int red;
	final int green;
	final int blue;

	public RGB(int red, int green, int blue) {
		super();
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blue;
		result = prime * result + green;
		result = prime * result + red;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RGB other = (RGB) obj;
		if (blue != other.blue)
			return false;
		if (green != other.green)
			return false;
		if (red != other.red)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "("+red+","+green+","+blue+")";
	}

	public static RGB fromString(String rgb){
		if(rgb != null && rgb.matches("\\(\\d+,\\d+,\\d+\\)")){
			rgb = rgb.substring(1, rgb.length()-1);
			String[] colors = rgb.split(",");
			return new RGB(
					Integer.parseInt(colors[0]),
					Integer.parseInt(colors[1]),
					Integer.parseInt(colors[2]));
		} else {
			throw new RuntimeException("Invalid RGB: "+rgb);
		}
	}
}
