using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace UserPreferences.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "UserPreferences",
                columns: table => new
                {
                    Id = table.Column<Guid>(type: "uuid", nullable: false),
                    UserId = table.Column<Guid>(type: "uuid", nullable: false),
                    AppType = table.Column<int>(type: "integer", nullable: false),
                    Theme = table.Column<int>(type: "integer", maxLength: 10, nullable: false),
                    UpdatedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_UserPreferences", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "HiddenAccounts",
                columns: table => new
                {
                    Id = table.Column<Guid>(type: "uuid", nullable: false),
                    UserPreferenceId = table.Column<Guid>(type: "uuid", nullable: false),
                    AccountId = table.Column<Guid>(type: "uuid", nullable: false),
                    HiddenAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_HiddenAccounts", x => x.Id);
                    table.ForeignKey(
                        name: "FK_HiddenAccounts_UserPreferences_UserPreferenceId",
                        column: x => x.UserPreferenceId,
                        principalTable: "UserPreferences",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_HiddenAccounts_UserPreferenceId_AccountId",
                table: "HiddenAccounts",
                columns: new[] { "UserPreferenceId", "AccountId" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_UserPreferences_UserId_AppType",
                table: "UserPreferences",
                columns: new[] { "UserId", "AppType" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "HiddenAccounts");

            migrationBuilder.DropTable(
                name: "UserPreferences");
        }
    }
}
