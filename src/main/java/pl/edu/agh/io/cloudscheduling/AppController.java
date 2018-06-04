package pl.edu.agh.io.cloudscheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import pl.edu.agh.io.cloudscheduling.broker.Broker;
import pl.edu.agh.io.cloudscheduling.entities.CloudResult;
import pl.edu.agh.io.cloudscheduling.entities.JSRequest;
import pl.edu.agh.io.cloudscheduling.entities.MCRequest;
import pl.edu.agh.io.cloudscheduling.tasks.JuliaSetTask;
import pl.edu.agh.io.cloudscheduling.tasks.MonteCarloTask;
import pl.edu.agh.io.cloudscheduling.utils.IDProvider;


@RestController
@RequestMapping("/app")
public class AppController{

    private Broker broker;

    @Autowired
    public void setBroker(Broker broker){
        this.broker = broker;
    }

    @PostMapping(value = "/montecarlo")
    public DeferredResult<ResponseEntity<CloudResult>> solveMonteCarlo(@RequestBody MCRequest mcRequest){
        DeferredResult<ResponseEntity<CloudResult>> result = new DeferredResult<>();

        MonteCarloTask task = new MonteCarloTask(IDProvider.getNewTaskId(), mcRequest);
        task.setResponseResult(result);

        broker.addTask(task);

        return result;
    }

    @PostMapping(value = "/juliaset")
    public DeferredResult<ResponseEntity<CloudResult>> drawJuliaSet(@RequestBody JSRequest jsRequest){
        DeferredResult<ResponseEntity<CloudResult>> result = new DeferredResult<>();
        JuliaSetTask task = new JuliaSetTask(IDProvider.getNewTaskId(), jsRequest);
        task.setResponseResult(result);

        broker.addTask(task);

        return result;
    }

    @GetMapping(value = "/vmId")
    public ResponseEntity<Long> getVMId(){
        return ResponseEntity.ok(IDProvider.getNewVMId());
    }
}
