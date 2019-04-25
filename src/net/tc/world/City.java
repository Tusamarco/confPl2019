package net.tc.world;
// Generated 24-Apr-2019 6:29:43 PM by Hibernate Tools 5.4.2.Final

/**
 * City generated by hbm2java
 */
public class City implements java.io.Serializable {

	private Integer id;
	private String name;
	private String countryCode;
	private String district;
	private int population;

	public City() {
	}

	public City(String name, String countryCode, String district, int population) {
		this.name = name;
		this.countryCode = countryCode;
		this.district = district;
		this.population = population;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return this.countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getDistrict() {
		return this.district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public int getPopulation() {
		return this.population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

}