package com.example.ljuba.trucks_client.helper.MapHelper;

import java.util.List;

/**
 * Created by Ljuba on 2.12.2017..
 */

public interface DirectionFinderListener {

    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);

}
