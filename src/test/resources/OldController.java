
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class OldController {
	
	@RequestMapping(method = RequestMethod.GET)
	public String getWithoutPath() {
		return "get";
	}
	
	@RequestMapping(path = "get", method = {GET})
	public String get() {
		return "get";
	}
	
	@RequestMapping(value = "get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getWithPathByValue() {
		return "get";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String postWithoutPath() {
		return "post";
	}
	
	@RequestMapping(path = "post", method = RequestMethod.POST)
	public String post() {
		return "post";
	}
	
	@RequestMapping(value = "post", method = {
		RequestMethod.POST
	})
	public String postWithPathByValue() {
		return "post";
	}
	
	@RequestMapping(method = RequestMethod.DELETE)
	public String deleteWithoutPath() {
		return "delete";
	}
	
	@RequestMapping(path = "delete", method = RequestMethod.DELETE)
	public String delete() {
		return "delete";
	}
	
	@RequestMapping(value = "delete", method = {
		RequestMethod.DELETE
	})
	public String deleteWithPathByValue() {
		return "delete";
	}
	
	@RequestMapping(method = RequestMethod.PUT)
	public String putWithoutPath() {
		return "put";
	}
	
	@RequestMapping(path = "put", method = RequestMethod.PUT)
	public String put() {
		return "put";
	}
	
	@RequestMapping(value = "put", method = {
		RequestMethod.PUT
	})
	public String putWithPathByValue() {
		return "put";
	}
	
	@RequestMapping(method = RequestMethod.PATCH)
	public String patchWithoutPath() {
		return "patch";
	}
	
	@RequestMapping(path = "patch", method = RequestMethod.PATCH)
	public String patch() {
		return "patch";
	}
	
	@RequestMapping(value = "patch", method = {
		RequestMethod.PATCH
	})
	public String patchWithPathByValue() {
		return "patch";
	}

}