package ServidorWeb;

@RestController
public class GreetingController {
    @GetMapping(value="/greeting", produces="text/plain")
    public String greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return "Hola " + name;
    }
}