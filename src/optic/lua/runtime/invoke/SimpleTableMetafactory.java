package optic.lua.runtime.invoke;

public class SimpleTableMetafactory implements TableMetafactory {
	public TableFactory create(int id) {
		return new SimpleTableFactory(id);
	}
}
