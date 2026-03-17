"""
Permission System - Filter context based on user role
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class PermissionFilter:
    """Filters data context based on user permissions"""
    
    @staticmethod
    def filter_context(context: Dict[str, Any], role: str, user_id: str) -> Dict[str, Any]:
        """
        Filter context data based on user role and ownership
        
        Args:
            context: Raw context from backend service
            role: User role (client, vendor, admin)
            user_id: ID of requesting user
            
        Returns:
            Filtered context appropriate for user's permission level
        """
        if role == "admin":
            # Admins see everything
            return context
        
        elif role == "vendor":
            # Vendors only see their own business data
            filtered = {}
            
            for key, value in context.items():
                # Check if this is vendor's own data
                if isinstance(value, dict):
                    if value.get("vendor_id") == user_id or value.get("business_owner_id") == user_id:
                        filtered[key] = value
                    else:
                        # Aggregate competitor data (no PII)
                        if "sales" in key or "price" in key:
                            filtered[key] = PermissionFilter._anonymize_competitor_data(value)
                elif isinstance(value, list):
                    # Filter list items
                    filtered_items = [
                        item for item in value
                        if isinstance(item, dict) and (
                            item.get("vendor_id") == user_id or
                            item.get("business_owner_id") == user_id
                        )
                    ]
                    if filtered_items:
                        filtered[key] = filtered_items
                else:
                    # Scalar values - include if not sensitive
                    if not any(sensitive in key.lower() for sensitive in ["password", "token", "secret", "phone", "email"]):
                        filtered[key] = value
            
            return filtered
        
        elif role == "client":
            # Clients see only public data, no business internals
            filtered = {}
            
            for key, value in context.items():
                # Only include public-facing data
                if any(public in key.lower() for public in [
                    "product", "service", "listing", "price", "description",
                    "location", "category", "rating", "review"
                ]):
                    # Remove internal IDs and metrics
                    if isinstance(value, dict):
                        filtered[key] = {
                            k: v for k, v in value.items()
                            if not any(internal in k.lower() for internal in [
                                "cost", "commission", "profit", "vendor_id", "internal"
                            ])
                        }
                    else:
                        filtered[key] = value
            
            return filtered
        
        else:
            logger.warning(f"Unknown role: {role}")
            return {}
    
    @staticmethod
    def _anonymize_competitor_data(data: Dict[str, Any]) -> Dict[str, Any]:
        """Remove PII from competitor data, keep aggregates"""
        anonymized = {}
        
        # Keep only aggregate metrics
        for key, value in data.items():
            if key in ["average_price", "market_average", "category_average", "trend"]:
                anonymized[key] = value
            elif "total" in key or "count" in key or "average" in key:
                anonymized[key] = value
        
        return anonymized
    
    @staticmethod
    def sanitize_response(response_text: str, role: str) -> str:
        """
        Sanitize AI response to ensure no unintended data leaks
        
        Args:
            response_text: Raw AI response
            role: User role
            
        Returns:
            Sanitized response
        """
        # For now, basic sanitization
        # In production, add more sophisticated PII detection
        
        sensitive_patterns = [
            "password", "secret", "api_key", "token"
        ]
        
        sanitized = response_text
        for pattern in sensitive_patterns:
            if pattern in sanitized.lower():
                logger.warning(f"Potential sensitive data in response: {pattern}")
                sanitized = sanitized.replace(pattern, "[REDACTED]")
        
        return sanitized


# Global instance
permission_filter = PermissionFilter()
