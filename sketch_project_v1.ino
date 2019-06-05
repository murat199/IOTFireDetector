#define ag_ismi "********"
#define ag_sifresi "********"


#define DHT11PIN 2 // DHT11PIN olarak Dijital 2"yi belirliyoruz.

#include <dht11.h> // dht11 kütüphanesini ekliyoruz.

dht11 DHT11;
#define IP "184.106.153.149"    //thingspeak.com IP adresi
 int mq3_analogPin = A0; //MQ3 ün çıkış pinine bağlı
 int mq135_analogPin = A1; //MQ3 ün çıkış pinine bağlı
float sicaklik;
 
void setup()
{
  Serial.begin(115200); //Seriport'u açıyoruz. Güncellediğimiz 
                        //ESP modülünün baudRate değeri 115200 olduğu için bizde Seriport'u 115200 şeklinde seçiyoruz
  
  Serial.println("AT"); //ESP modülümüz ile bağlantı kurulup kurulmadığını kontrol ediyoruz.
  
  delay(3000); //ESP ile iletişim için 3 saniye bekliyoruz.
 analogReference(INTERNAL);
  if(Serial.find("OK")){         //esp modülü ile bağlantıyı kurabilmişsek modül "AT" komutuna "OK" komutu ile geri dönüş yapıyor.
     Serial.println("AT+CWMODE=1"); //esp modülümüzün WiFi modunu STA şekline getiriyoruz. Bu mod ile modülümüz başka ağlara bağlanabilecek.
     delay(2000);
     String baglantiKomutu=String("AT+CWJAP=\"")+ag_ismi+"\",\""+ag_sifresi+"\"";
    Serial.println(baglantiKomutu);
    
     delay(5000);
 }
}
 
void loop(){
  /*float sicaklik = 9.31; 
 Serial.println(sicaklik);
 //sicaklik_yolla(sicaklik);
 int tempeture=getValueDHT11();
 int valueMq3=getMQ3();
 int valueMq135=getMQ135();
  
 sicaklik_yolla(tempeture,valueMq3,valueMq135);
 // dakikada 1 güncellenmesi için 1 dakika bekle
 delay(60000);
 */
 // okunan degerlerin dengelenmesi icin bol zaman verin
  int mq3_value = analogRead(mq3_analogPin);
  Serial.println(mq3_value);
  delay(100); //Cikisi yavaslatmak icin.
}

int getValueDHT11(){
   Serial.println();
  // Sensörün okunup okunmadığını konrol ediyoruz. 
  // chk 0 ise sorunsuz okunuyor demektir. Sorun yaşarsanız
  // chk değerini serial monitörde yazdırıp kontrol edebilirsiniz.
  int chk = DHT11.read(DHT11PIN);

  // Sensörden gelen verileri serial monitörde yazdırıyoruz.
  Serial.print("Nem (%): ");
  Serial.println((float)DHT11.humidity, 2);

  Serial.print("Sicaklik (Celcius): ");
  Serial.println((float)DHT11.temperature, 2);

  Serial.print("Sicaklik (Fahrenheit): ");
  Serial.println(DHT11.fahrenheit(), 2);

  Serial.print("Sicaklik (Kelvin): ");
  Serial.println(DHT11.kelvin(), 2);

  // Çiğ Oluşma Noktası, Dew Point
  Serial.print("Cig Olusma Noktasi: ");
  Serial.println(DHT11.dewPoint(), 2);
  return (int)DHT11.temperature;
 }

 int getMQ3(){
    int mq3_value = analogRead(mq3_analogPin);
  Serial.println("mq3_value:"+mq3_value);
  delay(100); //Cikisi yavaslatmak icin.
  return mq3_value;

  }

  int getMQ135(){
  
 int sensorValue = analogRead(mq135_analogPin);// read analog input pin 0
  Serial.print("AirQua=");
  Serial.print(sensorValue, DEC);// prints the value read
  Serial.println(" PPM");
  delay(100);// wait 100ms for next reading
  return sensorValue;
  }
void sicaklik_yolla(float sicaklik, int valMq3, int valueMq135){
 Serial.println(String("AT+CIPSTART=\"TCP\",\"") + IP + "\",80");  //thingspeak sunucusuna bağlanmak için bu kodu kullanıyoruz. 
                                                                   //AT+CIPSTART komutu ile sunucuya bağlanmak için sunucudan izin istiyoruz. 
                                                                   //TCP burada yapacağımız bağlantı çeşidini gösteriyor. 80 ise bağlanacağımız portu gösteriyor
 delay(1000);
  if(Serial.find("Error")){     //sunucuya bağlanamazsak ESP modülü bize "Error" komutu ile dönüyor.
   Serial.println("AT+CIPSTART Error");
    return;
  }
  
 String yollanacakkomut = "GET /update?key=*********************&field1=";   // Burada ********************* yazan kısım bizim API Key den aldığımız Key. Siz buraya kendi keyinizi yazacaksınız.
 yollanacakkomut += (int(sicaklik));                                      // Burada ise sıcaklığımızı float değişkenine atayarak yollanacakkomut değişkenine ekliyoruz.
 yollanacakkomut += "&field2=";   // Burada ********************* yazan kısım bizim API Key den aldığımız Key. Siz buraya kendi keyinizi yazacaksınız.
 yollanacakkomut += valMq3;
 yollanacakkomut += "&field3=";   // Burada ********************* yazan kısım bizim API Key den aldığımız Key. Siz buraya kendi keyinizi yazacaksınız.
 yollanacakkomut += valueMq135;
 yollanacakkomut += "\r\n\r\n";                                             // ESP modülümüz ile seri iletişim kurarken yazdığımız komutların modüle iletilebilmesi için Enter komutu yani
  delay(3000);                                                                                // /r/n komutu kullanmamız gerekiyor.
 
 Serial.print("AT+CIPSEND=");                    //veri yollayacağımız zaman bu komutu kullanıyoruz. Bu komut ile önce kaç tane karakter yollayacağımızı söylememiz gerekiyor.
 Serial.println(yollanacakkomut.length()+2);       //yollanacakkomut değişkeninin kaç karakterden oluştuğunu .length() ile bulup yazırıyoruz.
 
 delay(1000);
 
 if(Serial.find(">")){                           //eğer sunucu ile iletişim sağlayıp komut uzunluğunu gönderebilmişsek ESP modülü bize ">" işareti ile geri dönüyor.
                                                 // arduino da ">" işaretini gördüğü anda sıcaklık verisini esp modülü ile thingspeak sunucusuna yolluyor.
 Serial.print(yollanacakkomut);
 Serial.print("\r\n\r\n");
 }
 else{
 Serial.println("AT+CIPCLOSE");
 }
}
