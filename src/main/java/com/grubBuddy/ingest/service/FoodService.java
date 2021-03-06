package com.grubBuddy.ingest.service;

import com.grubBuddy.ingest.api.FoodListApi;
import com.grubBuddy.ingest.dao.FoodDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Component
public class FoodService {

    @Autowired
    private FoodListApi foodListApi;

    @Autowired
    private FoodDAO foodDAO;


    public void syncFoodsDatabase() throws InterruptedException {
        int page = 0;
        int take = 500;
        final boolean[] isDone = {false};
        final int[] inProcess = {0};

        while(!isDone[0]) {
            if(inProcess[0] != 5) {
                inProcess[0] = inProcess[0] + 1;
                this.foodDAO.insertMany(this.foodListApi.getFoods(take, page + 1)
                    .map(x -> {
                        if(x == null) {
                            isDone[0] = true;
                            return null;
                        };
                        return x.list.foodItems;
                    }))
                    .doOnComplete(() -> inProcess[0] = inProcess[0] - 1)
                    .doOnError(err -> isDone[0] = true)
                    .subscribeOn(Schedulers.parallel())
                    .subscribe();

                page += take;
            }
            Thread.sleep(0, 1);
        }
    }

}
