package org.github.tattoo.web;

import org.github.tattoo.TattooManager;
import org.github.tattoo.impl.singelgroup.model.TournamentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
public class TournamentController {

    @Autowired
    private TattooManager manager;

    @RequestMapping(value = "/tournament",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TournamentStatus> getStatus() {
        return manager.getStatus();
    }


    @RequestMapping(value = "/tournament",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void createTournament() {
//        manager.startTournament(new TournamentOptions());
    }

}