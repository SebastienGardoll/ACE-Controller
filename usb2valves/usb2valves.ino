#define BUFFER_SIZE 1
byte _buffer = B00000000;

// Code for ATmega328 (Arduino Nano 3.x) 05/02/2019.

// Register D: use pin from 7 to 2 (included), leave pin 1 & 0 unchanged.
// Register B: use pin from 9 to 8 (included), leave pin 13 to 10 (included)
// and cristal pins unchanged.

// Mapping between Isolation Valve number and pin number:
//  IV number : 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1
// pin number : 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2

// Register D mapping (x means not wired):
//   IV number : 6 | 5 | 4 | 3 | 2 | 1 | x | x
//  pin number : 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0
// bit position: 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0

// Register B mapping (x means not wired ; Xal means cristal pin):
//   IV number :  x  |  x  |  x |  x |  x |  x | 8 | 7
//  pin number : Xal | Xal | 13 | 12 | 11 | 10 | 9 | 8
// bit position:  7  |  6  |  5 |  4 |  3 |  2 | 1 | 0

void setup()
{
  // Don't assign PORTD before DDRD: it will change the setting of
  // the internal resistor of the board for the pin 0 (serial Rx).
  DDRD  |= B11111100; // Set pin 7 to 2 as OUTPUT.
  PORTD &= B00000011; // Set pin 7 to 2 to LOW.

  DDRB  |= B00000011; // Set pin 9 & 8 as OUTPUT.
  PORTB &= B11111100; // Set pin 9 & 8 to LOW.
  
  // Initialize serial and wait for port to open:
  Serial.begin(9600, SERIAL_8N1);
  while (!Serial)
  {
    ; // Wait for serial port to connect. Needed for native USB
  }
}

void loop()
{
  // Waiting for the next byte.
  if(Serial.available() > 0)
  {
    int nb_byte_read = Serial.readBytes(&_buffer, BUFFER_SIZE);

    if(nb_byte_read == BUFFER_SIZE)
    {
      process_byte(_buffer);
      Serial.println(_buffer);
    }
    else
    {
      Serial.println("E");
    }
  }
}

void process_byte(byte byte_read)
{
  {
    // Shift the bits so as to leave unchanged pin 1 & 0.
    byte byte_shifted = byte_read << 2;
    // Close the IV that are opened and has to be closed.
    // Don't close the IV that are opened and has to stay opened.
    PORTD &= (byte_shifted | B00000011);
    // Open the IV that was not opened and has to be opened.
    PORTD |= byte_shifted;
  }
  {
    // Shift the bits so as to leave unchanged the cristal pins
    // and pins from 13 to 10 (included).
    byte byte_shifted = byte_read >> 6;
    // Close the IV that are opened and has to be closed.
    // Don't close the IV that are opened and has to stay opened.
    PORTB &= (byte_shifted | B11111100);
    // Open the IV that was not opened and has to be opened.
    PORTB |= byte_shifted;
  }
}
