package com.revature.assignforce.web;


import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.revature.assignforce.annotations.Authorize;
import com.revature.assignforce.domain.*;

import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.client.OAuth2ClientContext;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import com.revature.assignforce.domain.dto.BatchDTO;
import com.revature.assignforce.domain.dto.ResponseErrorDTO;
import com.revature.assignforce.service.DaoService;


@RestController
@RequestMapping("/api/v2/batch")
@ComponentScan(basePackages = "com.revature.assignforce.service")
public class BatchCtrl {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	DaoService<Batch, Integer> batchService;

	@Autowired
	DaoService<Curriculum, Integer> currService;

	@Autowired
	DaoService<Location, Integer> locationService;

	@Autowired
	DaoService<Room, Integer> roomService;

	@Autowired
	DaoService<Trainer, Integer> trainerService;

	@Autowired
	DaoService<BatchLocation, Integer> batchLocationService;

	@Autowired
	DaoService<Unavailable, Integer> unavailableService;




	// CREATE
	// creating new batch object from information passed from batch data
	// transfer object
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Authorize
	public Object createBatch(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
							  @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
							  @RequestBody BatchDTO in) {

		int ID = in.getID();
		String name = in.getName();
		Curriculum curriculum = currService.getOneItem(in.getCurriculum());
		Curriculum focus = currService.getOneItem(in.getFocus());
		Trainer trainer = trainerService.getOneItem(in.getTrainer());
		Trainer cotrainer = trainerService.getOneItem(in.getCotrainer());
		Timestamp startDate = in.getStartDate();
		Timestamp endDate = in.getEndDate();
		BatchStatusLookup status = new BatchStatusLookup(1, "Scheduled");
		List<Skill> skills = in.getSkills();

		// Save Batch Location
		Integer tempBuilding = in.getBuilding();
		Integer tempRoom = in.getRoom();

		if (tempBuilding < 1) {
			tempBuilding = null;
		}
		if (tempRoom < 1) {
			tempRoom = null;
		}

		BatchLocation bl = new BatchLocation();
		bl.setLocationId(in.getLocation());
		bl.setBuildingId(tempBuilding);
		bl.setRoomId(tempRoom);

		bl = batchLocationService.saveItem(bl);

		// Save Unavailable
		Room room;
		if (tempRoom != null) {
			room = roomService.getOneItem(tempRoom);
		} else {
			room = null;
		}
		createUnavailabilities(trainer, room, startDate, endDate);

		Batch out = new Batch(ID, name, startDate, endDate, curriculum, status, trainer, cotrainer, skills, focus, bl);
		out = batchService.saveItem(out);

		if (out == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("Batch failed to save."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			return new ResponseEntity<Batch>(out, HttpStatus.OK);
		}
	}

	// RETRIEVE
	// retrieve batch with given ID
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object retrieveBatch(@CookieValue("JSESSIONID") String cookieSessionIdCookie,
								@RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
								@PathVariable("id") Integer ID) {

		Batch out = batchService.getOneItem(ID);
		if (out == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("No batch found of ID " + ID + "."),
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<Batch>(out, HttpStatus.OK);
		}
	}

	// DELETE
	// delete batch with given ID
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Authorize
	public Object deleteBatch(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
							  @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
							  @PathVariable("id") int ID) {
		Batch batch = batchService.getOneItem(ID);
		Timestamp startDate = batch.getStartDate();
		Timestamp endDate = batch.getEndDate();
		Trainer trainer = batch.getTrainer();
		BatchLocation batchLocation = batch.getBatchLocation();
		Integer roomId = batchLocation.getRoomId();
		Room room;
		if (roomId != null) {
			room = roomService.getOneItem(roomId);
		} else {
			room = null;
		}

		// Remove unavailabilities from trainer and room
		removeUnavailabilities(trainer, room, startDate, endDate);

		batchService.deleteItem(ID);

		return new ResponseEntity<Object>(null, HttpStatus.OK);
	}

	// GET ALL
	// retrieve all batches
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authorize
	public Object retrieveAllBatches(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
									 @RequestHeader(value="X-XSRF-TOKEN") String tokenValue) {

		List<Batch> all = batchService.getAllItems();
		if (all == null) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("Fetching all batches failed."),
					HttpStatus.NOT_FOUND);
		} else if (all.isEmpty()) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("No batches available."),
					HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<List<Batch>>(all, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Authorize
	public Object updateBatch(@CookieValue("JSESSIONID") String cookiesessionIdCookie,
							  @RequestHeader(value="X-XSRF-TOKEN") String tokenValue,
							  @RequestBody BatchDTO in) {
		// try to get batch from database
		Batch b = batchService.getOneItem(in.getID());

		if (b == null) {
			return new ResponseEntity<ResponseErrorDTO>(
					new ResponseErrorDTO("No batch with id '" + in.getID() + "' could be found to update"),
					HttpStatus.NOT_FOUND);
		}

		Integer oldRoomId = b.getBatchLocation().getRoomId();
		Trainer oldTrainer = b.getTrainer();
		Timestamp oldStartDate = b.getStartDate();
		Timestamp oldEndDate = b.getEndDate();

		b.setName(in.getName());
		b.setSkills(in.getSkills());
		b.setStartDate(in.getStartDate());
		b.setEndDate(in.getEndDate());

		if (in.getCurriculum() < 1) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO("Curriculum cannot be null"),
					HttpStatus.BAD_REQUEST);
		}
		Curriculum c = currService.getOneItem(in.getCurriculum());
		Curriculum f = currService.getOneItem(in.getFocus());

		b.setCurriculum(c);
		b.setFocus(f);

		Trainer t = trainerService.getOneItem(in.getTrainer());
		Trainer ct = trainerService.getOneItem(in.getCotrainer());

		b.setTrainer(t);
		b.setCotrainer(ct);

		BatchLocation bl = b.getBatchLocation();
		Integer tempLocationId = (in.getLocation() > 0 ? in.getLocation() : null);
		Integer tempBuildingId = (in.getBuilding() > 0 ? in.getBuilding() : null);
		Integer tempRoomId = (in.getRoom() > 0 ? in.getRoom() : null);

		bl.setLocationId(tempLocationId);
		bl.setBuildingId(tempBuildingId);
		bl.setRoomId(tempRoomId);
		b.setBatchLocation(bl);

		// Update unavailabilities for room and trainer
		Room oldRoom;
		if (oldRoomId != null) {
			oldRoom = roomService.getOneItem(oldRoomId);
		} else {
			oldRoom = null;
		}
		removeUnavailabilities(oldTrainer, oldRoom, oldStartDate, oldEndDate);

		Room room;
		if (tempRoomId != null) {
			room = roomService.getOneItem(tempRoomId);
		} else {
			room = null;
		}
		createUnavailabilities(t, room, b.getStartDate(), b.getEndDate());

		try {
			batchService.saveItem(b);
		} catch (Exception ex) {
			return new ResponseEntity<ResponseErrorDTO>(new ResponseErrorDTO(ex.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Batch>(b, HttpStatus.OK);
	}

	@Transactional
	void createUnavailabilities(Trainer trainer, Room room, Timestamp startDate, Timestamp endDate) {
		Unavailable unavailable = new Unavailable(startDate, endDate);
		List<Unavailable> unavailabilities;

		if (trainer != null) {
			unavailabilities = trainer.getUnavailabilities();
			unavailabilities.add(unavailable);
			trainer.setUnavailabilities(unavailabilities);
			trainerService.saveItem(trainer);
		}

		if (room != null) {
			unavailabilities = room.getUnavailabilities();
			unavailabilities.add(unavailable);
			room.setUnavailabilities(unavailabilities);
			roomService.saveItem(room);
		}
	}

	@Transactional
	void removeUnavailabilities(Trainer trainer, Room room, Timestamp startDate, Timestamp endDate) {
		Unavailable unavailableToRemove;
		List<Unavailable> unavailabilities;

		if (trainer != null) {
			int index = -1;
			unavailabilities = trainer.getUnavailabilities();
			for (int x = 0; x < unavailabilities.size(); x++) {
				Unavailable unavailable = unavailabilities.get(x);
				if (unavailable.getStartDate().equals(startDate) && unavailable.getEndDate().equals(endDate)) {
					index = x;
					break;
				}
			}
			if(index != -1){
				unavailableToRemove = unavailabilities.remove(index);
				unavailableService.deleteItem(unavailableToRemove.getID());
				trainer.setUnavailabilities(unavailabilities);
				trainerService.saveItem(trainer);
			}
		}

		if (room != null) {
			int index = -1;
			unavailabilities = room.getUnavailabilities();
			for (int x = 0; x < unavailabilities.size(); x++) {
				Unavailable unavailable = unavailabilities.get(x);
				if (unavailable.getStartDate().equals(startDate) && unavailable.getEndDate().equals(endDate)) {
					index = x;
					break;
				}
			}
			if(index != -1){
				unavailableToRemove = unavailabilities.remove(index);
				unavailableService.deleteItem(unavailableToRemove.getID());
				room.setUnavailabilities(unavailabilities);
				roomService.saveItem(room);
			}
		}
	}

}
