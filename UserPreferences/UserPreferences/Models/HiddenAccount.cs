namespace UserPreferences.Models
{
    public class HiddenAccount
    {
        public Guid Id { get; set; }
        public Guid UserPreferenceId { get; set; }
        public Guid AccountId { get; set; }
        public DateTime HiddenAt { get; set; }

        public UserPreference UserPreference { get; set; } = null!;
    }
}
