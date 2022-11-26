package BackEnd.Entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import BackEnd.*;

public class CloudController {

      private ArrayList<Vehicle> pendingVehicles;
      private ArrayList<Job> pendingJobs;

      public CloudController() {
            pendingVehicles = new ArrayList<Vehicle>();
            pendingJobs = new ArrayList<Job>();

            try {
                  getAllPendingVehicles();
            } catch (IOException e) {
                  e.printStackTrace();
            }

            try {
                  getAllPendingJobApps();
            } catch (IOException e) {
                  e.printStackTrace();
            }

            display();

      }

      public void display() {
            System.out.println("-------------------------------------------------------");
            System.out.println("Vehicle Apps");
            if (pendingVehicles.size() == 0) {
                  System.out.println("none");
            }
            for (int i = 0; i < pendingVehicles.size(); i++) {
                  System.out.println(pendingVehicles.get(i).toString());
            }
            System.out.println("Job Apps");
            if (pendingJobs.size() == 0) {
                  System.out.println("none");
            }
            for (int i = 0; i < pendingJobs.size(); i++) {
                  System.out.println(pendingJobs.get(i).toString());
            }
            System.out.println("-------------------------------------------------------");
      }

      public void addToPendingVehicles(Vehicle in) {
            pendingVehicles.add(in);
      }

      public void clearPendingVehicles() {
            pendingVehicles.clear();
      }

      public void addToPendingJobs(Job in) {
            pendingJobs.add(in);
      }

      public void clearPendingJobs() {
            pendingJobs.clear();
      }

      public String getFullJobTime() {
            int[] jobTimes = new int[pendingJobs.size()];
            jobTimes[0] = pendingJobs.get(0).getJobDuration();
            for (int i = 1; i < pendingJobs.size(); i++) {
                  jobTimes[i] = jobTimes[i - 1] + pendingJobs.get(i).getJobDuration();
            }
            String fullJobTime = Arrays.toString(jobTimes);
            return fullJobTime;
      }

      public String getAllJobIds() {
            int[] jobIds = new int[pendingJobs.size()];
            for (int i = 0; i < pendingJobs.size(); i++) {
                  jobIds[i] = pendingJobs.get(i).getJobID();
            }
            String allJobIds = Arrays.toString(jobIds);
            return allJobIds;
      }

      public ArrayList<Vehicle> getAllVehicles()
                  throws IOException {
            clearPendingVehicles();
            Scanner s = new Scanner(new File("GUI/Transcripts/allVehicles.txt"));
            ArrayList<String> list = new ArrayList<String>();
            while (s.hasNext()) {
                  list.add(s.next());
            }
            s.close();
            for (int i = 0; i < list.size(); i++) {

                  String pre = list.get(i);
                  String[] split = pre.split("/");
                  String make = split[0];
                  String model = split[1];
                  String yearStr = split[2];
                  String inStr = split[3];
                  String outStr = split[4];

                  Vehicle newVeh = new Vehicle(make, model, Integer.parseInt(yearStr), Integer.parseInt(inStr),
                              Integer.parseInt(outStr));
                  addToPendingVehicles(newVeh);
            }
            return pendingVehicles;
      }

      public ArrayList<Vehicle> getAllPendingVehicles()
                  throws IOException {
            clearPendingVehicles();
            Scanner s = new Scanner(new File("GUI/Transcripts/allPendingVehicleApps.txt"));
            ArrayList<String> list = new ArrayList<String>();
            while (s.hasNext()) {
                  list.add(s.next());
            }
            s.close();
            for (int i = 0; i < list.size(); i++) {

                  String pre = list.get(i);
                  String[] split = pre.split("/");
                  String make = split[0];
                  String model = split[1];
                  String yearStr = split[2];
                  String inStr = split[3];
                  String outStr = split[4];

                  Vehicle newVeh = new Vehicle(make, model, Integer.parseInt(yearStr), Integer.parseInt(inStr),
                              Integer.parseInt(outStr));
                  addToPendingVehicles(newVeh);
            }
            for (Vehicle v : pendingVehicles)
                  System.out.println(v);
            return pendingVehicles;
      }

      public ArrayList<Job> getAllPendingJobApps()
                  throws IOException {
            clearPendingJobs();
            Scanner s = new Scanner(new File("GUI/Transcripts/allPendingJobApps.txt"));
            ArrayList<String> list = new ArrayList<String>();
            while (s.hasNext()) {
                  list.add(s.next());
            }
            s.close();
            for (int i = 0; i < list.size(); i++) {

                  String pre = list.get(i);
                  String[] split = pre.split("/");
                  String name = split[0];
                  String type = split[1];
                  String duration = split[2];
                  String deadline = split[3];
                  String ID = split[4];

                  Job newJob = new Job(name, type, Integer.parseInt(duration), Integer.parseInt(deadline),
                              Integer.parseInt(ID));
                  addToPendingJobs(newJob);
            }
            for (Job j : pendingJobs)
                  System.out.println(j);
            return pendingJobs;

      }

      static ServerSocket serverSocket;
      static Socket socket;
      static DataInputStream inputStream;
      static DataOutputStream outputStream;

      public static void main(String[] args) {

            while (true) {

                  String messageIn = "";

                  try {

                        System.out.println("This is the server of of VCRTS");
                        System.out.println("wating for client to connect...");
                        try {
                              serverSocket = new ServerSocket(8000);
                        } catch (IOException e) {
                              e.printStackTrace();
                        }
                        while (true) {
                              try {
                                    socket = serverSocket.accept();
                              } catch (IOException e) {
                                    System.out.println("I/O error: " + e);
                              }

                              inputStream = new DataInputStream(socket.getInputStream());

                              messageIn = inputStream.readUTF();

                              System.out.println(messageIn);

                              String infoToBeAdded = messageIn;
                              Path file = FileSystems.getDefault().getPath("GUI/Transcripts/allPendingVehicleApps.txt");
                              File allVehiclesTranscript = file.toFile();

                              String str = "";
                              try {
                                    str = readFile(allVehiclesTranscript, StandardCharsets.UTF_8);
                              } catch (IOException e2) {
                                    e2.printStackTrace();
                              }

                              try {
                                    FileWriter regTranscript = new FileWriter(allVehiclesTranscript);
                                    regTranscript.write(str);
                                    regTranscript.write(infoToBeAdded + "\n");
                                    regTranscript.close();
                              } catch (IOException e1) {
                                    e1.printStackTrace();
                              }
                        }
                  } catch (Exception e) {

                        e.printStackTrace();
                  }
            }
      }

      public static String readFile(File file, Charset charset) throws IOException {
            return new String(Files.readAllBytes(file.toPath()), charset);
      }

}