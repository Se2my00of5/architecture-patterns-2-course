namespace UserPreferences.Models.DTOs
{
    public class UserPreferencesDto
    {
        public Guid UserId { get; set; }
        public AppType AppType { get; set; }
        public Theme Theme { get; set; } = Theme.Light;
        public List<Guid> HiddenAccountIds { get; set; } = new();
    }
}
