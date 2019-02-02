package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import android.graphics.Color;

@Autonomous(name="DepotTest", group="Autonomous")

public class DepotTest extends LinearOpMode {
    ArmHardwareOmni robot = new ArmHardwareOmni();

    public void runOpMode() {
        robot.init(hardwareMap);
        int targetPosition;
        telemetry.log().add("Gyro Calibrating. Do Not Move!");
        // Left motion motor 2 positive, motor 4 positive
        // Forward motion motor 1 positive motor 3 positive
        //Gyro calibating
        robot.modernRoboticsI2cGyro.calibrate();

        while (!isStopRequested() && robot.modernRoboticsI2cGyro.isCalibrating()) {
            telemetry.addData("calibrating", "%s", Math.round(robot.timer.seconds()) % 2 == 0 ? "|.." : "..|");
            telemetry.update();
            sleep(50);
        }

        //Finalized gyro calibration.
        telemetry.log().clear();
        telemetry.log().add("Gyro Calibrated. Press Start.");
        telemetry.clear();
        telemetry.update();

        //Initiate Servos
        robot.mascot.setPosition(0.8);//mascot up

        // Start Button
        waitForStart();
        robot.timer.reset();

        robot.frontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.backDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        robot.leftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.frontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.backDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Finalized startup

        //1. Lower Down and Clearance
        //Lifts the robot up to release latch tension
        runMotor(robot.verticalLift, 0.85, 1.8, "Lift Up");

        //Lowers the robot down
        runMotor(robot.verticalLift, -0.2, 1.7, "Lower Down");

        //2. Approach Sampling
        //Clears the latch
        runDriveTrain(robot.frontDrive, robot.backDrive, 0.25, 0.75, "Clearance");
        //Rotates to align
        rotateToTheta(1.0, 0, "Rotating");
        //Approaches the sampling field
        runDriveTrain(robot.leftDrive, robot.rightDrive, 0.25, 3, "Sample Approach");
        rotateToTheta(1.0, 0, "Rotating");
        runDriveTrain(robot.frontDrive, robot.backDrive, -0.25, 3, "Approach Initial Sample");

        //3-4. Sample Positioning and Sampling
        boolean sampleFound = false; //Whether the sample is found
        double sampleClearance = 5.0; //Time to clear sampling field
        double remainingSampleClearance = 0; //Time remaining after sampling
        rotateToTheta(1.0, 0, "Rotating");
        robot.frontDrive.setPower(0.25);
        robot.backDrive.setPower(0.25);
        while (opModeIsActive() && robot.timer.seconds() < sampleClearance && !checkColor()) {
            telemetry.addData("Sampling", "%2.5f S Elapsed", robot.timer.seconds());
            telemetry.update();
            sampleFound = checkColor();
        }
        remainingSampleClearance = sampleClearance - robot.timer.seconds();
        robot.timer.reset();
        if (sampleFound) {
            runDriveTrain(robot.leftDrive, robot.rightDrive, 0.25, 1.5, "Sampling");
            runDriveTrain(robot.leftDrive, robot.rightDrive, -0.25, 1.5, "Exiting Sampling");
            runDriveTrain(robot.frontDrive, robot.backDrive, 0.25, remainingSampleClearance, "Clearing Sampling");
        }
        //Clear of sampling

        //5. Rotate 45 degrees
        rotateToTheta(1.0, -135, "Rotating");

        //6. Approach Field Wall
        runDriveTrain(robot.leftDrive, robot.rightDrive, -0.25, 3, "Approaching Field Wall");

        //7. Approach Depot
        runDriveTrain(robot.frontDrive, robot.backDrive, 0.25, 3, "Approaching Depot");

        //8. Drop Mascot
        robot.mascot.setPosition(0.3);

        //9. Approach Crater
        runDriveTrain(robot.frontDrive, robot.backDrive, -0.25, 3, "Approaching Crater");

        //10. Rotate robot
        rotateToTheta(1.0, 45, "Rotating");

        //11. Extending arm over crater
        //Misnomer, runDriveTrain is used to run motor pair
        runDriveTrain(robot.leftExtension, robot.rightExtension, 0.25, 1, "Extending");

        telemetry.addData("Autonomous Completed", "Please Wait");
        telemetry.update();
    }

    public void runMotor(DcMotor motor, double power, double time, String label) {
        motor.setPower(power);
        while (opModeIsActive() && (robot.timer.seconds() < time)) {
            telemetry.addData(label, "%2.5f S Elapsed", robot.timer.seconds());
            telemetry.update();
        }
        motor.setPower(0);
        robot.timer.reset();
    }

    public void runDriveTrain(DcMotor motor1, DcMotor motor2, double power, double time, String label) {
        motor1.setPower(power);
        motor2.setPower(power);
        while (opModeIsActive() && (robot.timer.seconds() < time)) {
            telemetry.addData(label, "%2.5f S Elapsed", robot.timer.seconds());
            telemetry.update();
        }
        motor1.setPower(0);
        motor2.setPower(0);
        robot.timer.reset();
    }

    public void rotateToTheta(double time, double theta, String label) {
        while (opModeIsActive() && (robot.timer.seconds() < time) && getAngle() != theta) {
            robot.frontDrive.setPower(getRotationPower(theta));
            robot.backDrive.setPower(-getRotationPower(theta));
            robot.leftDrive.setPower(getRotationPower(theta));
            robot.rightDrive.setPower(-getRotationPower(theta));
            telemetry.addData(label, "%2.5f S Elapsed", robot.timer.seconds());
            telemetry.update();
        }
        robot.frontDrive.setPower(0);
        robot.backDrive.setPower(0);
        robot.leftDrive.setPower(0);
        robot.rightDrive.setPower(0);
        robot.timer.reset();
    }

    public float getAngle() {
        return robot.gyro.getAngularOrientation(robot.aRefInt, robot.aOrderXYZ, robot.aUnit).firstAngle;
    }

    public double getRotationPower(double theta) {
        double angle = getAngle();
        double power;
        if (!(Math.abs(theta - angle) < 1)) {
            power = 0.3 * Math.cbrt(angle/180);
        } else {
            power = 0;
        }
        return power;
    }
    public boolean checkColor() {
        robot.colors = robot.colorSensor.getNormalizedColors();
        float max = Math.max(Math.max(Math.max(robot.colors.red, robot.colors.green), robot.colors.blue), robot.colors.alpha);
        robot.colors.red   /= max;
        robot.colors.green /= max;
        robot.colors.blue  /= max;
        int color = robot.colors.toColor();
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return ((Math.abs(alpha - 70) < 75 ) &&
                (Math.abs(red - 255) < 75 ) &&
                (Math.abs(green - 107) < 75 ) &&
                (Math.abs(blue) < 30 ));
    }


}