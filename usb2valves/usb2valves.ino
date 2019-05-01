#define BUFFER_SIZE 1
byte _buffer = B00000000;
byte _mask   = B00000001;
int  _pins[] = {2, 3, 4, 5, 6, 7, 8, 9};

void setup()
{
  // Initialize the output pin and set them to LOW.
  for(int pin_position = 0 ; pin_position < 8 ; pin_position++)
  {
    pinMode(_pins[pin_position], OUTPUT);
    digitalWrite(_pins[pin_position], LOW);
  }
  
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
    // TODO: handle error.
    
    process_byte(_buffer);
    
    Serial.println(_buffer);
  }
}

void process_byte(byte byte_read)
{
  for(int bit_position = 0 ; bit_position < 8 ; bit_position++)
  {
    boolean bit_value = (byte_read & (_mask << bit_position)) != 0;
    process_pin(bit_position, bit_value);
  }
}

void process_pin(int pin_position, boolean is_on)
{
  if(is_on)
  {
    digitalWrite(_pins[pin_position], HIGH);
  }
  else
  {
    digitalWrite(_pins[pin_position], LOW);
  }
}
