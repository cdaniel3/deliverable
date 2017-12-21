package tech.corydaniel.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tech.corydaniel.exceptions.PriorityNotFoundException;
import tech.corydaniel.model.Priority;
import tech.corydaniel.repositories.PriorityRepository;

@RestController
@RequestMapping("/priorities")
public class PriorityRESTController {

	@Autowired
	private PriorityRepository priorityRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<Priority> getPriorities() {
		return getPriorityRepository().findAll();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/{priorityId}")
	public Priority getPriority(@PathVariable Long priorityId) {
		Priority priority = getPriorityRepository().findOne(priorityId);
		if (priority == null) throw new PriorityNotFoundException("Priority not found. Id: " + priorityId);
		return priority;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}
}
