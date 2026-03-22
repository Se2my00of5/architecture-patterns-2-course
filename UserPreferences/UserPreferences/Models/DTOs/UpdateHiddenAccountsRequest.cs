namespace UserPreferences.Models.DTOs
{
    public class UpdateHiddenAccountsRequest
    {
        public List<Guid> HiddenAccountIds { get; set; } = new();
    }
}
