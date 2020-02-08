#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

IPAddress apIP(192, 168, 105, 1);
ESP8266WebServer server(80);

void handleConfig();

void setup()
{
  Serial.begin(9600);
  Serial.println();

  Serial.print("Setting soft-AP ... ");
  WiFi.softAPConfig(apIP, apIP, IPAddress(255, 255, 255, 0));
  boolean result = WiFi.softAP("HevitraSensor", "HevitraPassword");
  if (result == true)
  {
    Serial.println("Ready");
    server.on("/config", handleConfig);
    server.begin();
    Serial.println("Server listening");
  }
  else
  {
    Serial.println("Failed!");
  }
}

void loop()
{
  server.handleClient();
}

void handleConfig() {
  server.send(200, "text/plain", "200: Trying to connect");

  String ssid = server.arg("ssid");
  String password = server.arg("password");

  WiFi.begin(ssid, password);
  Serial.print("Connecting to ");
  Serial.print(ssid); Serial.println(" ...");

  int i = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(++i); Serial.print(' ');
    if (i == 20)
    {
      Serial.println("Failed to connect");
      return;
    }
  }

  Serial.println('\n');
  Serial.println("Connection established!");
  Serial.print("IP address:\t");
  Serial.println(WiFi.localIP());
  WiFi.softAPdisconnect(true);
}
