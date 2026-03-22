namespace UserPreferences.Models
{
    public enum AppType
    {
        Client,
        Employee
    }

    public enum Theme
    {
        Light,
        Dark
    }

    public class UserPreference
    {
        public Guid Id { get; set; }
        public Guid UserId { get; set; }
        public AppType AppType { get; set; }
        public Theme Theme { get; set; } = Theme.Light;
        public DateTime UpdatedAt { get; set; }

        public ICollection<HiddenAccount> HiddenAccounts { get; set; } = new List<HiddenAccount>();
    }
}
