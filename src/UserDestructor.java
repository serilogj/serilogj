import serilogj.core.*;
import serilogj.events.StructureValue;

public class UserDestructor implements IDestructuringPolicy {
	private class UserDto {
		private UserDto(User user) {
			this.userName = user.getUserName();
		}

		@SuppressWarnings("unused")
		public String userName;
	}

	@Override
	public DestructuringPolicyResult tryDestructure(Object value, ILogEventPropertyValueFactory propertyValueFactory) {
		DestructuringPolicyResult result = new DestructuringPolicyResult();
		if (!(value instanceof User)) {
			return result;
		}

		result.isValid = true;
		result.result = propertyValueFactory.createPropertyValue(new UserDto((User) value), true);
		((StructureValue) result.result).setTypeTag("User");

		return result;
	}
}
