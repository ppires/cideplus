package cideplus.model;

public class Feature implements Comparable<Feature>{

	static final String FEATURE_PROPERTY_DELIMITER = "::";

	final Integer id;
	String name;
	RGB rgb;

	public Feature(Integer id){
		this.id = id;
		this.name = "Feature "+id;
		this.rgb = new RGB(0, 0, 0);
	}
	public Feature(Integer id, String name, RGB rgb){
		this.id = id;
		this.name = name;
		this.rgb = rgb;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRgb(RGB rgb) {
		this.rgb = rgb;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public RGB getRgb() {
		return rgb;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Feature other = (Feature) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id+FEATURE_PROPERTY_DELIMITER+name+FEATURE_PROPERTY_DELIMITER+rgb;
	}

	public int compareTo(Feature o) {
		return this.id.compareTo(o.id);
	}
}
