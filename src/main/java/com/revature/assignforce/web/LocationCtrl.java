package com.revature.assignforce.web;

import java.util.List;

import com.revature.assignforce.annotations.Authorize;
import com.revature.assignforce.service.ActivatableObjectDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.revature.assignforce.domain.Building;
import com.revature.assignforce.domain.Location;
import com.revature.assignforce.domain.dto.LocationDTO;
import com.revature.assignforce.domain.dto.ResponseErrorDTO;

@RestController
@RequestMapping("/api/v2/location")
@ComponentScan(basePackages = "com.revature.assignforce.service")
public class LocationCtrl {

	@Autowired
	ActivatableObjectDaoService<Location, Integer> locationService;

	// CREATE
	// creating new location object from information passed from location data
	// transfer object
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object createLocation(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
								 @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
								 @RequestBody LocationDTO in) {
		int ID = in.getID();
		String name = in.getName();
		String city = in.getCity();
		String state = in.getState();
		List<Building> buildings = in.getBuildings();

		// int iD, String name, String city, String state, List<Building>
		// buildings, Boolean active
		Location out = new Location(ID, name, city, state, buildings, true);
		out = locationService.saveItem(out);

		if (out == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("Location failed to save."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			return new ResponseEntity<Location>(out, HttpStatus.OK);
		}
	}

	// RETRIEVE
	// retrieve location with given ID
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object retrieveLocation(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
								   @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
								   @PathVariable("id") int ID) {
		Location out = locationService.getOneItem(ID);
		if (out == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("No location found of ID " + ID + "."),
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<Location>(out, HttpStatus.OK);
		}
	}

	// UPDATE
	// updating an existing location object with information passed from
	// location data transfer object
	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object updateLocation(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
								 @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
								 @RequestBody LocationDTO in) {
		int ID = in.getID();
		String name = in.getName();
		String city = in.getCity();
		String state = in.getState();
		List<Building> buildings = in.getBuildings();
		Boolean active = in.getActive();

		Location out = new Location(ID, name, city, state, buildings, active);
		out = locationService.saveItem(out);
		
		if (out == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("Location failed to update."),
					HttpStatus.NOT_MODIFIED);
		} else {
			return new ResponseEntity<Location>(out, HttpStatus.OK);
		}
	}

	// DELETE
	// delete location with given ID
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object deleteLocation(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
								 @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
								 @PathVariable("id") int ID) {
		locationService.deleteItem(ID);
		return new ResponseEntity<Object>(null, HttpStatus.OK);
	}

	// GET ALL
	// retrieve all locations
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object retrieveAllLocations(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
									   @RequestHeader(value="X-XSRF-TOKEN") String tokenValue) {
		List<Location> all = locationService.getAllItems();
		if (all == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("Fetching all locations failed."),
					HttpStatus.NOT_FOUND);
		} else if (all.isEmpty()) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("No locations available."),
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<List<Location>>(all, HttpStatus.OK);
		}
	}

}
