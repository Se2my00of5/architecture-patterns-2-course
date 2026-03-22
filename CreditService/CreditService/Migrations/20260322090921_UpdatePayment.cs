using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace CreditService.Migrations
{
    /// <inheritdoc />
    public partial class UpdatePayment : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "Type",
                table: "CreditPayments",
                newName: "Status");

            migrationBuilder.AddColumn<DateTime>(
                name: "DueDate",
                table: "CreditPayments",
                type: "timestamp with time zone",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "DueDate",
                table: "CreditPayments");

            migrationBuilder.RenameColumn(
                name: "Status",
                table: "CreditPayments",
                newName: "Type");
        }
    }
}
