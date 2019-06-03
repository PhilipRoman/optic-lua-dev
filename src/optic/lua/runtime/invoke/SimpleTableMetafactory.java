package optic.lua.runtime.invoke;

public final class SimpleTableMetafactory implements TableMetafactory {
	public SimpleTableMetafactory() {
	}

	public TableFactory create(int id) {
		return new SimpleTableFactory(id);
	}
}
