///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

import java.util.Hashtable;

public class VTyData extends VType {

    private String typeName;
    private Hashtable<String, VType> fields;

    public VTyData(String typeName) {
        super(CLTypes.DATA);
        this.typeName = typeName;
        this.fields = new Hashtable<>();
    }
    
    public VTyData(String typeName, Hashtable<String, VType> fields) {
        super(CLTypes.DATA);
        this.typeName = typeName;
        this.fields = fields;
    }

    public String getTypeName() {
        return typeName;
    }

    public Hashtable<String, VType> getFields() {
        return fields;
    }

    public void addField(String fieldName, VType fieldType) {
        fields.put(fieldName, fieldType);
    }

    public VType getFieldType(String fieldName) {
        return fields.get(fieldName);
    }

    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    @Override
    public boolean match(VType other) {
        if (other == null) return false;
        if (other.getTypeValue() != CLTypes.DATA) return false;

        VTyData otherData = (VTyData) other;
        return this.typeName.equals(otherData.typeName);
    }

    @Override
    public String toString() {
        return typeName;
    }
}
