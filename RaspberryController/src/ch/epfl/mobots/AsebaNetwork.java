package ch.epfl.mobots;
import java.util.List;
import org.freedesktop.DBus.Method.NoReply;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.UInt16;
import org.qtproject.QtDBus.QtTypeName.In1;
import org.qtproject.QtDBus.QtTypeName.In2;
import org.qtproject.QtDBus.QtTypeName.Out0;
public interface AsebaNetwork extends DBusInterface{

  @NoReply()
  public void LoadScripts(String fileName);
  
  public List<String> GetNodesList();
  
  public short GetNodeId(String node);
  
  public List<String> GetVariablesList(String node);
  
  @In2("Values")
  @NoReply()
  public void SetVariable(String node, String variable, List<Short> data);
  
  @Out0("Values")
  public List<Short> GetVariable(String node, String variable);
  
  @In1("Values")
  @NoReply()
  public void SendEvent(UInt16 event, List<Short> data);
  
  @In1("Values")
  @NoReply()
  public void SendEventName(String name, List<Short>  data);
  
  public DBusInterface CreateEventFilter();

}
