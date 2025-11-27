import static bje.hardware.mqtt.MetaDataUtil.KEY_MANAGED;
import static bje.hardware.mqtt.MetaDataUtil.KEY_TIMESTAMP;
import static bje.hardware.mqtt.MetaDataUtil.KEY_UPDATE_AUTHORIZED;

import java.io.StringReader;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import bje.hardware.listeners.Device;
import bje.hardware.listeners.TimedValue;
import bje.hardware.listeners.ValueListener;
import bje.hardware.mqtt.AbstractNormalizedDevice;
import bje.hardware.mqtt.Converter;
import bje.hardware.mqtt.Extractor;
import bje.hardware.mqtt.MQTTConnection;
import bje.hardware.mqtt.MessageSerializer;
import bje.toolbox.json.JSONElement;
import bje.toolbox.json.JSONObject;
import bje.toolbox.json.JSONType;
import bje.toolbox.json.JSONValue;
import bje.toolbox.json.parser.Parser;
import bje.toolbox.json.parser.ParserException;

public class MainClass {

}
