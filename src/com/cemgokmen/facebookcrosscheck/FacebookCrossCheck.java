/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cemgokmen.facebookcrosscheck;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Group;
import com.restfb.types.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author funstein
 */
public class FacebookCrossCheck {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FacebookClient facebookClient = new DefaultFacebookClient(""); // Must insert access token here!
        
        Set<String> wantedGroups = new HashSet<>();
        wantedGroups.add("O-MUN Middle East & Africa");
        wantedGroups.add("O-MUN Americas & Europe");
        wantedGroups.add("O-MUN Asia");
        
        Connection<Group> gr = facebookClient.fetchConnection("me/groups", Group.class, Parameter.with("limit", 1000));
        List<Group> grs = new ArrayList<>(gr.getData());
        
        Group mainGroup = null;
        Set<User> mainList = new HashSet<>();
        Set<User> missingList = new HashSet<>();
        Map<User, Set<Group>> userGroups = new HashMap<>();
        
        // First, find the main group
        for (Group g : grs) {
            String name = g.getName();
            if (name.toLowerCase().equals("o-mun delegates")) {
                mainGroup = g;
                
                String id = g.getId();
                Connection<User> mc = facebookClient.fetchConnection(id + "/members", User.class, Parameter.with("limit", 10000));
                List<User> members = mc.getData();
                
                for (User member : members) {
                    mainList.add(member);
                }
                
                break;
            }
        }
        
        grs.remove(mainGroup);
        
        for (Group g : grs) {
            String name = g.getName();
            if (wantedGroups.contains(name)) {
                String id = g.getId();
                Connection<User> mc = facebookClient.fetchConnection(id + "/members", User.class, Parameter.with("limit", 10000));
                List<User> members = mc.getData();
                
                int missingFound = 0;
                for (User member : members) {
                    if (!mainList.contains(member)) {
                        missingFound++;
                        missingList.add(member);
                        if (userGroups.get(member) == null) {
                            userGroups.put(member, new HashSet<>());
                        }
                        userGroups.get(member).add(g);
                    }
                }
                
                System.out.println(missingFound + " missing users found in group " + name);
            }
        }
        
        System.out.println();
        System.out.println("In total,");
        System.out.println(mainList.size() + " members present in Delegates group");
        System.out.println(missingList.size() + " members present in one of the listed groups but not the Delegates group");
        System.out.println();
        System.out.println("Those who aren't on the Delegates group are:");
        
        for (User missee : missingList) {
            System.out.println(missee.getName() + ", from group" + (userGroups.get(missee).size() == 1 ? "" : "s") + " " + concatGroups(userGroups.get(missee), ", "));
        }
    }
    
    public static String concatGroups(Iterable<Group> groups, String separator) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for(Group g: groups) {
            String s = g.getName();
            sb.append(sep).append(s);
            sep = separator;
        }
        return sb.toString();                           
    }
}
