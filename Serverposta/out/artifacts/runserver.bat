@ECHO OFF

"C:\Program Files\Java\jdk-13.0.1\bin\java.exe" --module-path C:/SW/javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.fxml -DlogLevel=5 --add-modules javafx.base,javafx.graphics --add-reads javafx.base=ALL-UNNAMED --add-reads javafx.graphics=ALL-UNNAMED  -Dfile.encoding=UTF-8 -classpath Serverposta.jar;Commons.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx-swt.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.base.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.controls.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.fxml.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.graphics.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.media.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.swing.jar;C:\SW\javafx-sdk-11.0.2\lib\javafx.web.jar servermail.Main
