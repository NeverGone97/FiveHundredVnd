package com.a3i.fivehundredvnd.model;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Anubis on 5/30/2017.
 */

public class FakeData {

    ArrayList<User1> listUser;
    ArrayList<Group1> listGroup;

    public FakeData() {
        listGroup = new ArrayList<>();
        listUser = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User1 user = new User1("vinicorp" + String.valueOf(i), i);
            listUser.add(user);
        }
        for (int j = 1; j < 6; j++) {
            Group1 group = new Group1("vinicorp_group" + String.valueOf(j), j);
            listGroup.add(group);
        }

    }

    public User1 getRandomUser() {
        int id = new Random().nextInt(10);
        return listUser.get(id);
    }


    public ArrayList<User1> getListUser() {
        return listUser;
    }

    public void setListUser(ArrayList<User1> listUser) {
        this.listUser = listUser;
    }

    public ArrayList<Group1> getListGroup() {
        return listGroup;
    }

    public void setListGroup(ArrayList<Group1> listGroup) {
        this.listGroup = listGroup;
    }


}
