package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;
import org.w3c.dom.html.HTMLOptGroupElement;


@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId=1;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name,String mobile){
        try{
            if(userMobile.contains(mobile)){throw new Exception("User already Exists");}
            else{
                User newUser=new User(name,mobile);
                userMobile.add(mobile);
            }
        }
        catch(Exception e){
            return e.getMessage();
        }
        return "SUCCESS";
    }

    public  Group createGroup(List<User> users){
        if(users.size()<=2){
            //personal chat
            Group group=new Group(users.get(1).getName(),2);
            adminMap.put(group,users.get(0));
            groupUserMap.put(group,users);
            return group;
        }
        else{
            String groupName="Group "+users.size();
            Group group=new Group(groupName,users.size());
            adminMap.put(group,users.get(0));
            groupUserMap.put(group,users);
            customGroupCount=customGroupCount+1;
            return group;
        }

    }

    public int createMessage(String content){
        Message message=new Message(messageId,content,new Date());
        messageId++;
        return messageId-1;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
            if(!groupUserMap.containsKey(group)){
                throw new Exception("Group does not Exist");
            }
            else{
                List<User> sendersList=groupUserMap.get(group);
                if(!sendersList.contains(sender))
                    throw new Exception("You are not allowed to send message");
                else {
                    senderMap.put(message,sender);
                    List<Message> messageList=groupMessageMap.get(group);
                    messageList.add(message);
                    groupMessageMap.put(group,messageList);
                    return messageList.size();
                }
            }
    }

    public String changeAdmin(User approver, User user,Group group) throws Exception{
            if(!groupUserMap.containsKey(group)){
                throw new Exception("Group does not Exist");
            }
            else{
                User admin=adminMap.get(group);
                List<User> participants=groupUserMap.get(group);
                if(!admin.equals(approver))
                    throw new Exception("Approver does not have rights");
                else if(!participants.contains(user))
                    throw new Exception("User is not a participant");
                else {
                    adminMap.put(group,user);
                    return "SUCCESS";
                }
            }
    }


    public int removeUser(User user) throws Exception {
        int count=0;
        Group mainGroup=null;
        for(Group group:groupUserMap.keySet()){
            List<User> groupUserList=groupUserMap.get(group);
            if(groupUserList.contains(user)){
                mainGroup=group;
                count++;
                if(adminMap.get(group).equals(user)) throw new Exception("Cannot remove admin");
            }
        }
        if(count!=1) throw new Exception("User Not Found");
        else{
            List<User> userList=groupUserMap.get(mainGroup);
            userList.remove(user);
            groupUserMap.put(mainGroup,userList);
            ArrayList<Message> messageList=new ArrayList<>();
            for(Message message:senderMap.keySet()){
                if(senderMap.get(message).equals(user)) {
                    messageList.add(message);
                }
            }
            for(Message message:messageList){
                senderMap.remove(message);
                groupMessageMap.get(mainGroup).remove(message);
            }
        }
        return groupUserMap.get(mainGroup).size()+groupMessageMap.get(mainGroup).size()+senderMap.size();
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        ArrayList<Message> messageList=new ArrayList<>();
        for(Message message:senderMap.keySet()){
            if(message.getTimestamp().compareTo(start)>0 && message.getTimestamp().compareTo(end)<0) {
                messageList.add(message);
            }
        }
        if(k>messageList.size()) throw new Exception("K is greater than the number of messages");
        else return messageList.get(k-1).getContent();
    }
}
