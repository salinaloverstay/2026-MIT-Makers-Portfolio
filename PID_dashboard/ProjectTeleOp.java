package org.firstinspires.ftc.teamcode.qubit.teleOps;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.qubit.core.FtcLogger;
import org.firstinspires.ftc.teamcode.qubit.core.FtcUtils;
import org.firstinspires.ftc.teamcode.qubit.core.TrollBots.BaseBot;


import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;


import com.pedropathing.follower.Follower;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.qubit.core.FtcLogger;
import org.firstinspires.ftc.teamcode.qubit.core.FtcUtils;
import org.firstinspires.ftc.teamcode.qubit.core.TrollBots.BaseBot;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;




//@Disabled
@TeleOp(name = "Project TeleOp", group = "Debug")
public class ProjectTeleOp extends LinearOpMode {
   // Declare OpMode members
   ElapsedTime runtime = new ElapsedTime();
   Follower follower;


   @Override
   public void runOpMode() {


       follower = Constants.createFollower(hardwareMap);


       waitForStart();


       while (opModeIsActive()) {


           follower.update();


           double x = getRobotX();
           double y = getRobotY();
           double heading = getHeading();
           double velocity = getVelocity();
           double error = getPidError();
           double Herror = getHeadingError();




           // Send data every ~200ms
           if (runtime.milliseconds() % 200 < 20) {
               sendTelemetry(x, y, heading, velocity, error, Herror);
           }


           telemetry.addData("X", x);
           telemetry.addData("Y", y);
           telemetry.addData("Heading", heading);
           telemetry.addData("Vel", velocity);
           telemetry.addData("Error", error);
           telemetry.addData("Heading Error", Herror);
           telemetry.update();
       }
   }


   public void sendTelemetry(double x, double y, double heading,
                             double velocity, double error, double Herror) {


       try {


           telemetry.addLine("Sending telemetry");
           telemetry.update();


           URL url = new URL("http://192.168.43.22:5000/data");


           HttpURLConnection conn = (HttpURLConnection) url.openConnection();


           conn.setRequestMethod("POST");
           conn.setRequestProperty("Content-Type", "application/json");
           conn.setDoOutput(true);


           String json = String.format("{\"x\": %.2f, \"y\": %.2f, \"heading\": %.2f, \"velocity\": %.2f, \"error\": %.2f}",
                   x, y, heading, velocity, error, Herror);


           OutputStream os = conn.getOutputStream();
           os.write(json.getBytes());
           os.flush();
           os.close();


           conn.getResponseCode();


           conn.disconnect();


       } catch (Exception e) {
           telemetry.addData("Network Error", e.getMessage());
       }
       telemetry.update();
   }


   public double getRobotX() {
       return follower.getPose().getX();
   }


   public double getRobotY() {
       return follower.getPose().getY();
   }


   public double getHeading() {
       return follower.getPose().getHeading(); // radians
   }


   double lastX = 0;
   double lastY = 0;
   double lastTime = 0;


   public double getVelocity() {
       double currentX = getRobotX();
       double currentY = getRobotY();
       double currentTime = runtime.seconds();


       double dx = currentX - lastX;
       double dy = currentY - lastY;
       double dt = currentTime - lastTime;


       lastX = currentX;
       lastY = currentY;
       lastTime = currentTime;


       if (dt == 0) return 0;


       return Math.hypot(dx, dy) / dt;
   }


   public double getPidError() {
       double targetX = 50;
       double targetY = 50;


       double dx = targetX - getRobotX();
       double dy = targetY - getRobotY();


       return Math.hypot(dx, dy);
   }


   public double getHeadingError() {
       double targetHeading = Math.PI/2;
       double Herror = targetHeading - getHeading();


       // Wrap to [-π, π]
       while (Herror > Math.PI) Herror -= 2 * Math.PI;
       while (Herror < -Math.PI) Herror += 2 * Math.PI;


       return Herror;
   }


}
